package com.databit.stockia.platform.stockmanagement.application.internal.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.LocalizedText;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.stockmanagement.application.commands.CreateInventoryItemCommand;
import com.databit.stockia.platform.stockmanagement.application.commands.DeductStockCommand;
import com.databit.stockia.platform.stockmanagement.application.commands.DeleteInventoryItemCommand;
import com.databit.stockia.platform.stockmanagement.application.commands.UpdateInventoryItemCommand;
import com.databit.stockia.platform.stockmanagement.application.commandservices.InventoryItemCommandService;
import com.databit.stockia.platform.stockmanagement.application.commandservices.StockManagementError;
import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;
import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.StockConsumption;
import com.databit.stockia.platform.stockmanagement.domain.repositories.InventoryItemRepository;
import com.databit.stockia.platform.stockmanagement.domain.repositories.StockConsumptionRepository;
import com.databit.stockia.platform.stockmanagement.domain.services.StockAllocationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Add, modify and remove supplies and take out what a sale consumes. Every stock change publishes
 * StockLevelChangedEvent for Notifications &amp; Messaging.
 */
@Service
public class InventoryItemCommandServiceImpl implements InventoryItemCommandService {
    private final InventoryItemRepository inventoryItemRepository;
    private final StockConsumptionRepository stockConsumptionRepository;
    private final RestaurantClock clock;
    private final ApplicationEventPublisher eventPublisher;

    public InventoryItemCommandServiceImpl(InventoryItemRepository inventoryItemRepository,
                                           StockConsumptionRepository stockConsumptionRepository, RestaurantClock clock,
                                           ApplicationEventPublisher eventPublisher) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockConsumptionRepository = stockConsumptionRepository;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Result<InventoryItem, CommandFailure> handle(CreateInventoryItemCommand command) {
        InventoryItem item;
        try {
            item = InventoryItem.create(command.restaurantId(), command.name(), command.unit(), command.quantity(),
                    command.minThreshold(), command.storageType(), command.shelfLifeDays(), command.unitCost(), clock.today());
        } catch (IllegalArgumentException exception) {
            return Result.failure(invalidData(exception));
        }
        return Result.success(saveAndPublish(item));
    }

    @Override
    @Transactional
    public Result<InventoryItem, CommandFailure> handle(UpdateInventoryItemCommand command) {
        var found = inventoryItemRepository.findById(command.inventoryItemId())
                .filter(item -> item.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty()) return Result.failure(CommandFailure.of(StockManagementError.INVENTORY_ITEM_NOT_FOUND));
        var item = found.get();
        try {
            item.update(command.name(), command.unit(), command.quantity(), command.minThreshold(), command.storageType(),
                    command.shelfLifeDays(), command.unitCost(), clock.today());
        } catch (IllegalArgumentException exception) {
            return Result.failure(invalidData(exception));
        }
        return Result.success(saveAndPublish(item));
    }

    @Override
    @Transactional
    public Result<Void, CommandFailure> handle(DeleteInventoryItemCommand command) {
        var found = inventoryItemRepository.findById(command.inventoryItemId())
                .filter(item -> item.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty()) return Result.failure(CommandFailure.of(StockManagementError.INVENTORY_ITEM_NOT_FOUND));
        inventoryItemRepository.delete(found.get());
        return Result.success(null);
    }

    /**
     * Takes out what a sold dish consumes: freshest usable lot first, never an expired lot. Fails
     * without changing anything when stock is short, and records each lot used as a StockConsumption.
     */
    @Override
    @Transactional
    public Result<Void, CommandFailure> handle(DeductStockCommand command) {
        var items = inventoryItemRepository.findAllByRestaurantId(command.restaurantId());
        var today = clock.today();
        var plan = StockAllocationService.plan(command.requirements(), items, command.portions(), today);
        if (!plan.canFulfill())
            return Result.failure(CommandFailure.of(StockManagementError.INSUFFICIENT_STOCK).withDetails(plan.shortages()));
        for (var deduction : plan.deductions()) {
            stockConsumptionRepository.save(StockConsumption.create(command.restaurantId(), deduction.item(), deduction.taken(), today));
            deduction.item().deduct(deduction.taken());
            saveAndPublish(deduction.item());
        }
        return Result.success(null);
    }

    private InventoryItem saveAndPublish(InventoryItem item) {
        var saved = inventoryItemRepository.save(item);
        saved.registerStockChange(clock.instant());
        saved.pullDomainEvents().forEach(eventPublisher::publishEvent);
        return saved;
    }

    private static CommandFailure invalidData(IllegalArgumentException exception) {
        return CommandFailure.of(StockManagementError.INVALID_DATA, LocalizedText.of(exception.getMessage()));
    }
}
