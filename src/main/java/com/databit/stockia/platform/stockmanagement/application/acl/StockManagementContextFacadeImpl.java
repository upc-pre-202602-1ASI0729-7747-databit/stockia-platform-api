package com.databit.stockia.platform.stockmanagement.application.acl;

import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.stockmanagement.application.commands.DeductStockCommand;
import com.databit.stockia.platform.stockmanagement.application.commandservices.InventoryItemCommandService;
import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.Shortage;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StockRequirement;
import com.databit.stockia.platform.stockmanagement.domain.repositories.InventoryItemRepository;
import com.databit.stockia.platform.stockmanagement.domain.repositories.StockConsumptionRepository;
import com.databit.stockia.platform.stockmanagement.domain.services.StockAllocationService;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StockManagementContextFacadeImpl implements StockManagementContextFacade {
    private final InventoryItemRepository inventoryItemRepository;
    private final StockConsumptionRepository stockConsumptionRepository;
    private final InventoryItemCommandService inventoryItemCommandService;
    private final RestaurantClock clock;

    public StockManagementContextFacadeImpl(InventoryItemRepository inventoryItemRepository,
                                            StockConsumptionRepository stockConsumptionRepository,
                                            InventoryItemCommandService inventoryItemCommandService, RestaurantClock clock) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockConsumptionRepository = stockConsumptionRepository;
        this.inventoryItemCommandService = inventoryItemCommandService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public StockAvailability checkAvailability(Long restaurantId, List<IngredientRequirement> ingredients, int portions) {
        var items = inventoryItemRepository.findAllByRestaurantId(restaurantId);
        var requirements = toRequirements(ingredients);
        var plan = StockAllocationService.plan(requirements, items, portions, clock.today());
        return new StockAvailability(plan.canFulfill(), StockAllocationService.priceOf(requirements, items, clock.today()),
                plan.shortages().stream().map(StockManagementContextFacadeImpl::toShortageInfo).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BigDecimal> pricesOf(Long restaurantId, List<List<IngredientRequirement>> dishes) {
        var items = inventoryItemRepository.findAllByRestaurantId(restaurantId);
        return dishes.stream().map(dish -> StockAllocationService.priceOf(toRequirements(dish), items, clock.today())).toList();
    }

    @Override
    @Transactional
    public List<ShortageInfo> deductStock(Long restaurantId, List<IngredientRequirement> ingredients, int portions) {
        var result = inventoryItemCommandService.handle(new DeductStockCommand(restaurantId, toRequirements(ingredients), portions));
        if (result.isSuccess()) return List.of();
        var failure = result.failure().orElseThrow();
        if (failure.details() instanceof List<?> shortages)
            return shortages.stream().filter(Shortage.class::isInstance).map(Shortage.class::cast)
                    .map(StockManagementContextFacadeImpl::toShortageInfo).toList();
        return List.of(new ShortageInfo("", "", "NOT_FOUND", "0", "0", "0"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryLotInfo> getInventory(Long restaurantId) {
        return inventoryItemRepository.findAllByRestaurantId(restaurantId).stream().map(this::toLotInfo).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryLotInfo> getInventoryLot(Long restaurantId, Long inventoryItemId) {
        return inventoryItemRepository.findById(inventoryItemId).filter(item -> item.getRestaurantId().equals(restaurantId))
                .map(this::toLotInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockConsumptionInfo> getConsumptions(Long restaurantId, LocalDate from, LocalDate to) {
        return stockConsumptionRepository.findAllByRestaurantIdBetween(restaurantId, from, to).stream()
                .map(consumption -> new StockConsumptionInfo(consumption.getConsumedOn(), consumption.getItemName(),
                        consumption.getUnit(), consumption.getQuantity().value(), consumption.value(),
                        consumption.daysBeforeExpiration()))
                .toList();
    }

    private static List<StockRequirement> toRequirements(List<IngredientRequirement> ingredients) {
        return ingredients.stream()
                .map(line -> new StockRequirement(line.inventoryItemId(), line.itemName(), line.unit(), line.quantityPerPortion()))
                .toList();
    }

    private InventoryLotInfo toLotInfo(InventoryItem item) {
        var today = clock.today();
        return new InventoryLotInfo(item.getId(), item.getName(), item.getUnit(), item.getQuantity().value(),
                item.getMinThreshold().value(), item.getExpirationDate(), item.daysToExpire(today), item.getUnitCost(),
                item.stockValue(), item.statusOn(today).name(), item.productKey());
    }

    private static ShortageInfo toShortageInfo(Shortage shortage) {
        return new ShortageInfo(shortage.itemName(), shortage.unit(), shortage.reason().name(), shortage.required().toString(),
                shortage.available().toString(), shortage.expired().toString());
    }
}
