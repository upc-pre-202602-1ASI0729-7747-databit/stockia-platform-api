package com.databit.stockia.platform.recipesmanagement.application.acl;

import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.recipesmanagement.application.internal.outboundservices.ExternalStockService;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Sale;
import com.databit.stockia.platform.recipesmanagement.domain.model.entities.SaleLineItem;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.RecipeRepository;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.SaleRepository;
import com.databit.stockia.platform.recipesmanagement.interfaces.acl.RecipesManagementContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class RecipesManagementContextFacadeImpl implements RecipesManagementContextFacade {
    private final RecipeRepository recipeRepository;
    private final SaleRepository saleRepository;
    private final ExternalStockService externalStockService;
    private final RestaurantClock clock;

    public RecipesManagementContextFacadeImpl(RecipeRepository recipeRepository, SaleRepository saleRepository,
                                              ExternalStockService externalStockService, RestaurantClock clock) {
        this.recipeRepository = recipeRepository;
        this.saleRepository = saleRepository;
        this.externalStockService = externalStockService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeInfo> getActiveRecipes(Long restaurantId) {
        var recipes = recipeRepository.findAllByRestaurantId(restaurantId).stream().filter(recipe -> recipe.isActive()).toList();
        if (recipes.isEmpty()) return List.of();
        var prices = externalStockService.pricesOf(restaurantId, recipes);
        var lots = externalStockService.getInventory(restaurantId);
        return java.util.stream.IntStream.range(0, recipes.size()).mapToObj(index -> {
            var recipe = recipes.get(index);
            var ingredients = recipe.getIngredients().stream().map(line -> new RecipeIngredientInfo(line.inventoryItemId(),
                    line.inventoryItemName(), line.unit(), line.quantityRequired().value(),
                    lots.stream().filter(lot -> lot.inventoryItemId().equals(line.inventoryItemId())).map(lot -> lot.productKey())
                            .findFirst().orElse(productKeyOf(line.inventoryItemName(), line.unit())))).toList();
            return new RecipeInfo(recipe.getId(), recipe.getDishName(), prices.get(index), ingredients);
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyDishSales> getDailyUnitsByDish(Long restaurantId, LocalDate from, LocalDate to) {
        Map<LocalDate, Map<String, Integer>> units = new TreeMap<>();
        for (var sale : confirmedSales(restaurantId, from, to)) {
            var day = clock.localDateOf(sale.getSaleDate());
            for (SaleLineItem line : sale.getLineItems())
                units.computeIfAbsent(day, ignored -> new TreeMap<>()).merge(line.dishName(), line.quantity(), Integer::sum);
        }
        return units.entrySet().stream().flatMap(day -> day.getValue().entrySet().stream()
                .map(dish -> new DailyDishSales(day.getKey(), dish.getKey(), dish.getValue())))
                .sorted(Comparator.comparing(DailyDishSales::date)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesSummary getSummary(Long restaurantId) {
        var confirmed = saleRepository.findAllByRestaurantId(restaurantId, null, null).stream().filter(Sale::isConfirmed).toList();
        var today = clock.today();
        return new SalesSummary(confirmed.size(), sum(confirmed),
                sum(confirmed.stream().filter(sale -> clock.localDateOf(sale.getSaleDate()).equals(today)).toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public SalesPeriodSummary getSalesBetween(Long restaurantId, LocalDate from, LocalDate to) {
        var sales = confirmedSales(restaurantId, from, to);
        return new SalesPeriodSummary(from, to, sales.size(), sales.stream().mapToInt(Sale::portions).sum(), sum(sales));
    }

    private List<Sale> confirmedSales(Long restaurantId, LocalDate from, LocalDate to) {
        return saleRepository.findAllByRestaurantId(restaurantId, clock.startOf(from), clock.endOf(to)).stream()
                .filter(Sale::isConfirmed).toList();
    }

    private static BigDecimal sum(List<Sale> sales) {
        return sales.stream().map(Sale::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Same normalization as Stock Management, for an ingredient whose lot was deleted.
     */
    static String productKeyOf(String name, String unit) {
        return normalize(name) + "|" + normalize(unit);
    }

    private static String normalize(String text) {
        return String.join(" ", text.trim().toLowerCase(Locale.ROOT).split("\\s+"));
    }
}
