package com.databit.stockia.platform.mlrecommendations.application.internal.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.mlrecommendations.application.commands.ApplyRecommendationCommand;
import com.databit.stockia.platform.mlrecommendations.application.commands.GenerateRecommendationsCommand;
import com.databit.stockia.platform.mlrecommendations.application.commandservices.MlRecommendationsError;
import com.databit.stockia.platform.mlrecommendations.application.commandservices.RecommendationCommandService;
import com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices.acl.ExternalOperationsService;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.Recommendation;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.DishPlan;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.IngredientNeed;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.ProductStock;
import com.databit.stockia.platform.mlrecommendations.domain.repositories.DemandForecastRepository;
import com.databit.stockia.platform.mlrecommendations.domain.repositories.RecommendationRepository;
import com.databit.stockia.platform.mlrecommendations.domain.services.RecommendationPolicy;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade.InventoryLotInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Menu Recommendation Engine (C4): purchase and menu recommendations from a demand forecast
 * (RecommendationPolicy). The pending recommendations of the previous forecast are replaced; applied
 * ones are kept as history.
 */
@Service
public class RecommendationCommandServiceImpl implements RecommendationCommandService {
    private final RecommendationRepository recommendationRepository;
    private final DemandForecastRepository demandForecastRepository;
    private final ExternalOperationsService externalOperationsService;
    private final RestaurantClock clock;

    public RecommendationCommandServiceImpl(RecommendationRepository recommendationRepository, DemandForecastRepository demandForecastRepository,
                                            ExternalOperationsService externalOperationsService, RestaurantClock clock) {
        this.recommendationRepository = recommendationRepository;
        this.demandForecastRepository = demandForecastRepository;
        this.externalOperationsService = externalOperationsService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public List<Recommendation> handle(GenerateRecommendationsCommand command) {
        var found = demandForecastRepository.findById(command.demandForecastId())
                .filter(forecast -> forecast.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty()) return List.of();
        var forecast = found.get();
        var recipes = externalOperationsService.getActiveRecipes(command.restaurantId());
        var inventory = externalOperationsService.getInventory(command.restaurantId());
        var today = clock.today();
        var lastWeek = externalOperationsService.getDailyUnitsByDish(command.restaurantId(), today.minusDays(7), today.minusDays(1));
        var projections = forecast.unitsByDish().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).toList();
        var dishes = new ArrayList<DishPlan>();
        for (var projection : projections) {
            var recipe = recipes.stream().filter(candidate -> candidate.dishName().equals(projection.getKey())).findFirst();
            var ingredients = recipe.map(info -> info.ingredients().stream()
                    .map(line -> new IngredientNeed(line.productKey(), line.itemName(), line.unit(), line.quantityRequired())).toList()).orElse(List.of());
            var soldLastWeek = lastWeek.stream().filter(row -> row.dishName().equals(projection.getKey())).mapToInt(row -> row.units()).sum();
            dishes.add(new DishPlan(projection.getKey(), projection.getValue(), soldLastWeek, ingredients));
        }
        var stock = productStock(inventory);
        recommendationRepository.findPendingByRestaurantId(command.restaurantId()).forEach(recommendationRepository::delete);
        var horizonEnd = forecast.getDataPoints().stream().map(point -> point.date()).max(Comparator.naturalOrder())
                .orElse(clock.localDateOf(forecast.getGeneratedAt()));
        var now = clock.instant();
        return RecommendationPolicy.propose(dishes, stock, horizonEnd).stream()
                .map(proposal -> recommendationRepository.save(Recommendation.create(command.restaurantId(), proposal.type(), proposal.message(),
                        proposal.expectedImpact(), forecast.getId(), now)))
                .toList();
    }

    @Override
    @Transactional
    public Result<Recommendation, CommandFailure> handle(ApplyRecommendationCommand command) {
        var found = recommendationRepository.findById(command.recommendationId())
                .filter(recommendation -> recommendation.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty()) return Result.failure(CommandFailure.of(MlRecommendationsError.RECOMMENDATION_NOT_FOUND));
        return Result.success(recommendationRepository.save(found.get().apply()));
    }

    /**
     * Usable quantity (lots not expired) of each product, in the order the lots are listed.
     */
    private static List<ProductStock> productStock(List<InventoryLotInfo> inventory) {
        var groups = new LinkedHashMap<String, List<InventoryLotInfo>>();
        inventory.forEach(lot -> groups.computeIfAbsent(lot.productKey(), ignored -> new ArrayList<>()).add(lot));
        return groups.entrySet().stream().map(group -> new ProductStock(group.getKey(), group.getValue().getFirst().name(),
                group.getValue().getFirst().unit(), group.getValue().stream().filter(lot -> !"EXPIRED".equals(lot.status()))
                .map(InventoryLotInfo::quantity).reduce(BigDecimal.ZERO, BigDecimal::add))).toList();
    }
}
