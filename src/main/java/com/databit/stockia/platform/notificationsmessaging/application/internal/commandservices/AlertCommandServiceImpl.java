package com.databit.stockia.platform.notificationsmessaging.application.internal.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.LocalizedText;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.notificationsmessaging.application.commands.*;
import com.databit.stockia.platform.notificationsmessaging.application.commandservices.AlertCommandService;
import com.databit.stockia.platform.notificationsmessaging.application.commandservices.NotificationsMessagingError;
import com.databit.stockia.platform.notificationsmessaging.application.internal.outboundservices.AlertMessageResolver;
import com.databit.stockia.platform.notificationsmessaging.application.internal.outboundservices.NotificationSender;
import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.StockSnapshot;
import com.databit.stockia.platform.notificationsmessaging.domain.repositories.AlertRepository;
import com.databit.stockia.platform.notificationsmessaging.domain.services.InventoryAlertPolicy;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade.InventoryLotInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AlertCommandServiceImpl implements AlertCommandService {
    private final AlertRepository alertRepository;
    private final NotificationSender notificationSender;
    private final AlertMessageResolver messageResolver;
    private final StockManagementContextFacade stockManagementContextFacade;
    private final RestaurantClock clock;

    public AlertCommandServiceImpl(AlertRepository alertRepository, NotificationSender notificationSender, AlertMessageResolver messageResolver,
                                   StockManagementContextFacade stockManagementContextFacade, RestaurantClock clock) {
        this.alertRepository = alertRepository;
        this.notificationSender = notificationSender;
        this.messageResolver = messageResolver;
        this.stockManagementContextFacade = stockManagementContextFacade;
        this.clock = clock;
    }

    /**
     * A manual alert; its main channel counts as the first delivery attempt.
     */
    @Override
    @Transactional
    public Result<Alert, CommandFailure> handle(CreateAlertCommand command) {
        Alert alert;
        try {
            alert = Alert.createManual(command.restaurantId(), command.type(), command.severity(), command.message(), command.channel(), clock.instant());
        } catch (IllegalArgumentException exception) {
            return Result.failure(CommandFailure.of(NotificationsMessagingError.INVALID_DATA, LocalizedText.of(exception.getMessage())));
        }
        if (notificationSender.send(command.restaurantId(), command.channel(), alert.getMessage())) alert.registerDelivery(command.channel());
        return Result.success(alertRepository.save(alert));
    }

    @Override
    @Transactional
    public Result<Alert, CommandFailure> handle(UpdateAlertCommand command) {
        var found = find(command.alertId(), command.restaurantId());
        if (found.isEmpty()) return notFound();
        try {
            return Result.success(alertRepository.save(found.get().update(command.type(), command.severity(), command.message(), command.channel())));
        } catch (IllegalArgumentException exception) {
            return Result.failure(CommandFailure.of(NotificationsMessagingError.INVALID_DATA, LocalizedText.of(exception.getMessage())));
        }
    }

    @Override
    @Transactional
    public Result<Void, CommandFailure> handle(DeleteAlertCommand command) {
        var found = find(command.alertId(), command.restaurantId());
        if (found.isEmpty()) return Result.failure(CommandFailure.of(NotificationsMessagingError.ALERT_NOT_FOUND));
        alertRepository.delete(found.get());
        return Result.success(null);
    }

    /**
     * Only delivered alerts (every required channel) can be marked as attended.
     */
    @Override
    @Transactional
    public Result<Alert, CommandFailure> handle(AcknowledgeAlertCommand command) {
        var found = find(command.alertId(), command.restaurantId());
        if (found.isEmpty()) return notFound();
        var alert = found.get();
        if (!alert.acknowledge()) return Result.failure(CommandFailure.of(NotificationsMessagingError.NOT_DELIVERED));
        return Result.success(alertRepository.save(alert));
    }

    @Override
    @Transactional
    public Result<Alert, CommandFailure> handle(RetryAlertDeliveryCommand command) {
        var found = find(command.alertId(), command.restaurantId());
        if (found.isEmpty()) return notFound();
        var alert = found.get();
        var pending = alert.pendingChannel();
        if (pending.isEmpty()) return Result.failure(CommandFailure.of(NotificationsMessagingError.ALREADY_DELIVERED));
        if (!notificationSender.send(alert.getRestaurantId(), pending.get(), messageResolver.textOf(alert, Locale.ENGLISH)))
            return Result.failure(CommandFailure.of(NotificationsMessagingError.DELIVERY_FAILED));
        return Result.success(alertRepository.save(alert.registerDelivery(pending.get())));
    }

    /**
     * Automatic alerts (InventoryAlertPolicy). An open alert of the same supply and type is refreshed
     * instead of repeated; new ones are sent by their main channel (critical ones by WhatsApp and email).
     */
    @Override
    @Transactional
    public int handle(EvaluateInventoryAlertsCommand command) {
        List<InventoryLotInfo> lots = command.inventoryItemId() == null
                ? stockManagementContextFacade.getInventory(command.restaurantId())
                : stockManagementContextFacade.getInventoryLot(command.restaurantId(), command.inventoryItemId()).map(List::of).orElse(List.of());
        var created = 0;
        for (var lot : lots) {
            var snapshot = new StockSnapshot(lot.inventoryItemId(), lot.name(), lot.unit(), lot.quantity(), lot.minThreshold(), lot.daysToExpire(),
                    lot.status());
            for (var proposal : InventoryAlertPolicy.evaluate(snapshot)) {
                var open = alertRepository.findOpen(command.restaurantId(), lot.inventoryItemId(), proposal.type());
                if (open.isPresent()) {
                    alertRepository.save(open.get().refresh(proposal.type(), proposal.severity(), proposal.messageKey(), proposal.messageArgs(),
                            proposal.channel()));
                    continue;
                }
                var alert = Alert.createAutomatic(command.restaurantId(), lot.inventoryItemId(), proposal.type(), proposal.severity(),
                        proposal.messageKey(), proposal.messageArgs(), proposal.channel(), clock.instant());
                var text = messageResolver.textOf(alert, Locale.ENGLISH);
                for (var channel : alert.requiredChannels())
                    if (notificationSender.send(command.restaurantId(), channel, text)) alert.registerDelivery(channel);
                alertRepository.save(alert);
                created++;
            }
        }
        return created;
    }

    private Optional<Alert> find(Long alertId, Long restaurantId) {
        return alertRepository.findById(alertId).filter(alert -> alert.getRestaurantId().equals(restaurantId));
    }

    private static Result<Alert, CommandFailure> notFound() {
        return Result.failure(CommandFailure.of(NotificationsMessagingError.ALERT_NOT_FOUND));
    }
}