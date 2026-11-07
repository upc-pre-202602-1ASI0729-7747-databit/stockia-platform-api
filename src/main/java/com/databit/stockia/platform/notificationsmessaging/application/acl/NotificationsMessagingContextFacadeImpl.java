package com.databit.stockia.platform.notificationsmessaging.application.acl;

import com.databit.stockia.platform.notificationsmessaging.application.commands.EvaluateInventoryAlertsCommand;
import com.databit.stockia.platform.notificationsmessaging.application.commandservices.AlertCommandService;
import com.databit.stockia.platform.notificationsmessaging.application.internal.outboundservices.NotificationSender;
import com.databit.stockia.platform.notificationsmessaging.domain.repositories.AlertRepository;
import com.databit.stockia.platform.notificationsmessaging.interfaces.acl.NotificationsMessagingContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationsMessagingContextFacadeImpl implements NotificationsMessagingContextFacade {
    private final AlertRepository alertRepository;
    private final AlertCommandService alertCommandService;
    private final NotificationSender notificationSender;

    public NotificationsMessagingContextFacadeImpl(AlertRepository alertRepository, AlertCommandService alertCommandService,
                                                   NotificationSender notificationSender) {
        this.alertRepository = alertRepository;
        this.alertCommandService = alertCommandService;
        this.notificationSender = notificationSender;
    }

    @Override
    @Transactional(readOnly = true)
    public int countPendingAlerts(Long restaurantId) {
        return (int) alertRepository.countUnacknowledged(restaurantId);
    }

    @Override
    public void evaluateInventoryAlerts(Long restaurantId) {
        alertCommandService.handle(new EvaluateInventoryAlertsCommand(restaurantId, null));
    }

    @Override
    public boolean sendEmail(Long restaurantId, String to, String subject, String body) {
        return notificationSender.sendEmail(restaurantId, to, subject, body);
    }

    @Override
    public boolean isEmailSimulated() {
        return notificationSender.isSimulated();
    }
}