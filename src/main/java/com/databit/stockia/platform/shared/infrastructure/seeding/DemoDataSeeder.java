package com.databit.stockia.platform.shared.infrastructure.seeding;

import com.databit.stockia.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;
import com.databit.stockia.platform.iam.domain.repositories.UserRepository;
import com.databit.stockia.platform.mlrecommendations.application.commands.GenerateDemandForecastCommand;
import com.databit.stockia.platform.mlrecommendations.application.commandservices.DemandForecastCommandService;
import com.databit.stockia.platform.mlrecommendations.domain.services.PeruCalendar;
import com.databit.stockia.platform.notificationsmessaging.interfaces.acl.NotificationsMessagingContextFacade;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Recipe;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Sale;
import com.databit.stockia.platform.recipesmanagement.domain.model.entities.RecipeIngredient;
import com.databit.stockia.platform.recipesmanagement.domain.model.entities.SaleLineItem;
import com.databit.stockia.platform.recipesmanagement.domain.model.valueobjects.SaleChannel;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.RecipeRepository;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.SaleRepository;
import com.databit.stockia.platform.restaurantregistration.domain.model.aggregates.Restaurant;
import com.databit.stockia.platform.restaurantregistration.domain.repositories.RestaurantRepository;
import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;
import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.StockConsumption;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StockRequirement;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StorageType;
import com.databit.stockia.platform.stockmanagement.domain.repositories.InventoryItemRepository;
import com.databit.stockia.platform.stockmanagement.domain.repositories.StockConsumptionRepository;
import com.databit.stockia.platform.stockmanagement.domain.services.StockAllocationService;
import com.databit.stockia.platform.subscriptionpayments.interfaces.acl.SubscriptionContextFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Loads the demo restaurant of the web application when stockia.seed.demo-data is true and the demo
 * administrator does not exist yet: its two accounts, supplies, recipes, 120 days of sales history (so
 * the ML forecast works from day one), the consumption log of the last two weeks, automatic alerts and
 * a first forecast with recommendations. Dates are relative to the seeding day. It runs after the plan
 * catalog is loaded and writes through the domain repositories of each bounded context.
 */
@Slf4j
@Component
public class DemoDataSeeder {
    public static final String DEMO_PASSWORD = "stockia123";
    public static final String ADMIN_EMAIL = "admin@databitecorp.com";
    public static final String EMPLOYEE_EMAIL = "empleado@databitecorp.com";

    private final boolean enabled;
    private final TransactionTemplate transactionTemplate;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final SubscriptionContextFacade subscriptionContextFacade;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockConsumptionRepository stockConsumptionRepository;
    private final RecipeRepository recipeRepository;
    private final SaleRepository saleRepository;
    private final NotificationsMessagingContextFacade notificationsMessagingContextFacade;
    private final DemandForecastCommandService demandForecastCommandService;
    private final RestaurantClock clock;

    public DemoDataSeeder(@Value("${stockia.seed.demo-data:false}") boolean enabled, PlatformTransactionManager transactionManager,
                          RestaurantRepository restaurantRepository, UserRepository userRepository, HashingService hashingService,
                          SubscriptionContextFacade subscriptionContextFacade, InventoryItemRepository inventoryItemRepository,
                          StockConsumptionRepository stockConsumptionRepository, RecipeRepository recipeRepository, SaleRepository saleRepository,
                          NotificationsMessagingContextFacade notificationsMessagingContextFacade,
                          DemandForecastCommandService demandForecastCommandService, RestaurantClock clock) {
        this.enabled = enabled;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.subscriptionContextFacade = subscriptionContextFacade;
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockConsumptionRepository = stockConsumptionRepository;
        this.recipeRepository = recipeRepository;
        this.saleRepository = saleRepository;
        this.notificationsMessagingContextFacade = notificationsMessagingContextFacade;
        this.demandForecastCommandService = demandForecastCommandService;
        this.clock = clock;
    }

    @Order(10)
    @EventListener
    public void on(ApplicationReadyEvent event) {
        if (!enabled || userRepository.existsByEmail(ADMIN_EMAIL)) return;
        var restaurantId = transactionTemplate.execute(status -> seedDemoRestaurant());
        if (restaurantId == null) return;
        try {
            notificationsMessagingContextFacade.evaluateInventoryAlerts(restaurantId);
            demandForecastCommandService.handle(new GenerateDemandForecastCommand(restaurantId));
        } catch (RuntimeException exception) {
            log.warn("The demo alerts or the first demand forecast could not be generated", exception);
        }
        log.info("Demo restaurant {} ready: sign in as {} or {} with password {}", restaurantId, ADMIN_EMAIL, EMPLOYEE_EMAIL, DEMO_PASSWORD);
    }

    private Long seedDemoRestaurant() {
        var today = clock.today();
        log.info("Seeding the demo restaurant ({})", today);
        var restaurant = restaurantRepository.save(Restaurant.create("DataBite Corp - Demo Restaurant")
                .updateProfile("DataBite Corp - Demo Restaurant", "Av. Jose Larco 123, Miraflores, Lima", "+51 987 654 321"));
        var restaurantId = restaurant.getId();
        var admin = userRepository.save(User.create("Carla Gallardo", ADMIN_EMAIL, hashingService.encode(DEMO_PASSWORD), restaurantId, UserRole.ADMIN));
        userRepository.save(User.create("Jesus Miranda", EMPLOYEE_EMAIL, hashingService.encode(DEMO_PASSWORD), restaurantId, UserRole.EMPLOYEE));
        restaurantRepository.save(restaurant.linkOwner(admin.getId()));
        subscriptionContextFacade.createDefaultSubscription(restaurantId);

        // An expired chicken lot and a fresh one: sales must use the fresh lot.
        var expiredChicken = item(restaurantId, "Chicken breast", "kg", "18", "10", StorageType.REFRIGERATED, 4, "14.5", today, today.minusDays(3));
        var flour = item(restaurantId, "Wheat flour", "kg", "32", "15", StorageType.AMBIENT, 180, "3.2", today, null);
        var cheese = item(restaurantId, "Mozzarella cheese", "kg", "4", "6", StorageType.REFRIGERATED, 10, "22", today, null);
        var tomato = item(restaurantId, "Tomato", "kg", "9", "8", StorageType.AMBIENT, 6, "4.1", today, today.plusDays(1));
        var beef = item(restaurantId, "Beef tenderloin", "kg", "0", "5", StorageType.FROZEN, 30, "32", today, null);
        var lettuce = item(restaurantId, "Lettuce", "kg", "6", "4", StorageType.REFRIGERATED, 5, "3.5", today, null);
        var potato = item(restaurantId, "Potato", "kg", "40", "20", StorageType.AMBIENT, 45, "2.1", today, null);
        var freshChicken = item(restaurantId, "Chicken breast", "kg", "12.5", "10", StorageType.REFRIGERATED, 4, "15", today, null);
        // A product whose only lot is expired: its dish cannot be sold.
        var cream = item(restaurantId, "Heavy cream", "L", "3", "1", StorageType.REFRIGERATED, 7, "12", today, today.minusDays(2));
        var pasta = item(restaurantId, "Fettuccine", "kg", "5", "2", StorageType.AMBIENT, 120, "8", today, null);
        var items = List.of(expiredChicken, flour, cheese, tomato, beef, lettuce, potato, freshChicken, cream, pasta);

        var recipes = List.of(
                recipeRepository.save(Recipe.create(restaurantId, "Margherita Pizza", List.of(line(flour, "0.3"), line(cheese, "0.2"), line(tomato, "0.15")))),
                recipeRepository.save(Recipe.create(restaurantId, "Lomo Saltado", List.of(line(beef, "0.25"), line(potato, "0.3"), line(tomato, "0.1")))),
                recipeRepository.save(Recipe.create(restaurantId, "Chicken Caesar Salad", List.of(line(freshChicken, "0.18"), line(lettuce, "0.12")))),
                recipeRepository.save(Recipe.create(restaurantId, "Fettuccine Alfredo", List.of(line(pasta, "0.15"), line(cream, "0.1")))));
        var recentSales = seedSalesHistory(restaurantId, recipes, items, today);
        seedConsumptionLog(restaurantId, recipes, items, recentSales);
        return restaurantId;
    }

    private InventoryItem item(Long restaurantId, String name, String unit, String quantity, String min, StorageType storage, int shelfLife,
                               String cost, LocalDate today, LocalDate expirationDate) {
        var item = InventoryItem.create(restaurantId, name, unit, Quantity.parse(quantity), Quantity.parse(min), storage, shelfLife,
                new BigDecimal(cost), today);
        if (expirationDate != null) item.overrideExpirationDate(expirationDate);
        return inventoryItemRepository.save(item);
    }

    private static RecipeIngredient line(InventoryItem item, String quantity) {
        return new RecipeIngredient(item.getId(), item.getName(), Quantity.parse(quantity), item.getUnit());
    }

    private static List<StockRequirement> requirementsOf(Recipe recipe) {
        return recipe.getIngredients().stream()
                .map(line -> new StockRequirement(line.inventoryItemId(), line.inventoryItemName(), line.unit(), line.quantityRequired())).toList();
    }

    /**
     * 120 days of daily POS sales with the same patterns the model was trained on (weekday, holidays,
     * quincena) plus noise. Historic sales do not discount stock.
     *
     * @return the sales of the last 14 days with their local day
     */
    private Map<LocalDate, Sale> seedSalesHistory(Long restaurantId, List<Recipe> recipes, List<InventoryItem> items, LocalDate today) {
        var random = new Random(2026);
        double[] weekdayFactors = {0.80, 0.85, 0.90, 0.95, 1.15, 1.35, 1.20};
        // Fettuccine Alfredo is new on the menu: it has no sales history yet.
        Map<String, Double> baseUnits = Map.of("Margherita Pizza", 28d, "Lomo Saltado", 32d, "Chicken Caesar Salad", 22d);
        var recent = new LinkedHashMap<LocalDate, Sale>();
        for (var daysBack = 120; daysBack >= 1; daysBack--) {
            var day = today.minusDays(daysBack);
            var factor = weekdayFactors[PeruCalendar.mondayBasedDayOfWeek(day)];
            if (PeruCalendar.isHoliday(day)) factor *= 1.2;
            else if (PeruCalendar.isHolidayEve(day)) factor *= 1.07;
            if (PeruCalendar.isPaydayWindow(day)) factor *= 1.12;
            var lines = new ArrayList<SaleLineItem>();
            for (var recipe : recipes) {
                var base = baseUnits.get(recipe.getDishName());
                if (base == null) continue;
                var noise = Math.sqrt(-2 * Math.log(1 - random.nextDouble())) * Math.cos(2 * Math.PI * random.nextDouble());
                var units = (int) Math.max(0, Math.round(base * factor * (1 + 0.18 * noise)));
                if (units > 0) lines.add(new SaleLineItem(recipe.getId(), recipe.getDishName(),
                        StockAllocationService.priceOf(requirementsOf(recipe), items, today), units));
            }
            if (lines.isEmpty()) continue;
            var saleDate = day.atTime(LocalTime.of(21, 0)).atZone(clock.zone()).toInstant();
            var sale = saleRepository.save(Sale.create(restaurantId, saleDate, SaleChannel.POS, lines));
            if (daysBack <= 14) recent.put(day, sale);
        }
        return recent;
    }

    /**
     * Demo consumption log of the last two weeks (what those sales took from each lot), so the weekly
     * savings of Analytics &amp; Dashboard have data.
     */
    private void seedConsumptionLog(Long restaurantId, List<Recipe> recipes, List<InventoryItem> items, Map<LocalDate, Sale> recentSales) {
        for (var entry : recentSales.entrySet()) {
            var day = entry.getKey();
            for (var line : entry.getValue().getLineItems()) {
                var recipe = recipes.stream().filter(candidate -> candidate.getId().equals(line.recipeId())).findFirst().orElseThrow();
                for (var ingredient : recipe.getIngredients()) {
                    var lot = items.stream().filter(item -> item.getId().equals(ingredient.inventoryItemId())).findFirst().orElseThrow();
                    if (lot.isExpired(day)) continue;
                    stockConsumptionRepository.save(StockConsumption.create(restaurantId, lot, ingredient.quantityRequired().times(line.quantity()), day));
                }
            }
        }
    }
}