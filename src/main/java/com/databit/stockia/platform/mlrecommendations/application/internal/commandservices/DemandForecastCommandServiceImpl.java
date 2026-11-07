package com.databit.stockia.platform.mlrecommendations.application.internal.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.mlrecommendations.application.commands.GenerateDemandForecastCommand;
import com.databit.stockia.platform.mlrecommendations.application.commandservices.DemandForecastCommandService;
import com.databit.stockia.platform.mlrecommendations.application.commandservices.MlRecommendationsError;
import com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices.DemandModel;
import com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices.WeatherForecastService;
import com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices.acl.ExternalOperationsService;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.DemandForecast;
import com.databit.stockia.platform.mlrecommendations.domain.model.entities.ForecastDataPoint;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.DailyWeather;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.ForecastMethod;
import com.databit.stockia.platform.mlrecommendations.domain.repositories.DemandForecastRepository;
import com.databit.stockia.platform.mlrecommendations.domain.services.DemandFeatureBuilder;
import com.databit.stockia.platform.mlrecommendations.domain.services.WeatherConditionClassifier;
import com.databit.stockia.platform.recipesmanagement.interfaces.acl.RecipesManagementContextFacade.RecipeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * ForecastGenerationService. For each active dish and each of the next days it builds the features
 * (sales history from Recipes Management, Peruvian calendar, weather forecast) and asks the ONNX model
 * for the expected units. With less than 14 days of sales, or without a model, it uses the
 * same-weekday baseline.
 */
@Slf4j
@Service
public class DemandForecastCommandServiceImpl implements DemandForecastCommandService {
    /**
     * Days with sales needed (out of the last 35) to trust the model.
     */
    public static final int MINIMUM_HISTORY_DAYS = 14;

    private record FeatureRow(DailyWeather day, String dish, float[] features) {
    }

    private final DemandForecastRepository repository;
    private final DemandModel demandModel;
    private final WeatherForecastService weatherForecastService;
    private final ExternalOperationsService externalOperationsService;
    private final RestaurantClock clock;
    private final ApplicationEventPublisher eventPublisher;

    public DemandForecastCommandServiceImpl(DemandForecastRepository repository, DemandModel demandModel,
                                            WeatherForecastService weatherForecastService, ExternalOperationsService externalOperationsService,
                                            RestaurantClock clock, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.demandModel = demandModel;
        this.weatherForecastService = weatherForecastService;
        this.externalOperationsService = externalOperationsService;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Result<DemandForecast, CommandFailure> handle(GenerateDemandForecastCommand command) {
        if (command.horizonDays() < 1 || command.horizonDays() > DemandFeatureBuilder.MINIMUM_LAG_DAYS)
            return Result.failure(CommandFailure.of(MlRecommendationsError.INVALID_HORIZON));
        var dishes = externalOperationsService.getActiveRecipes(command.restaurantId()).stream().map(RecipeInfo::dishName).distinct().toList();
        if (dishes.isEmpty()) return Result.failure(CommandFailure.of(MlRecommendationsError.NO_DISHES));
        var today = clock.today();
        var historyFrom = today.minusDays(DemandFeatureBuilder.HISTORY_DAYS_REQUIRED + 1);
        var sales = externalOperationsService.getDailyUnitsByDish(command.restaurantId(), historyFrom, today.minusDays(1));
        Map<String, Map<LocalDate, Double>> history = new HashMap<>();
        dishes.forEach(dish -> history.put(dish, new HashMap<>()));
        var daysWithSalesSet = new HashSet<LocalDate>();
        for (var row : sales) {
            daysWithSalesSet.add(row.date());
            var dishHistory = history.get(row.dishName());
            if (dishHistory != null) dishHistory.put(row.date(), (double) row.units());
        }
        var daysWithSales = daysWithSalesSet.size();
        var coverage = Math.min(1.0, daysWithSales / (double) (DemandFeatureBuilder.HISTORY_DAYS_REQUIRED + 1));
        var location = externalOperationsService.getRestaurantLocation(command.restaurantId());
        var weather = weatherForecastService.getDailyForecast(location.latitude(), location.longitude(), today, command.horizonDays());
        var rows = new ArrayList<FeatureRow>();
        for (var day : weather)
            for (var dish : dishes)
                rows.add(new FeatureRow(day, dish, DemandFeatureBuilder.build(history.get(dish), day.date(), day.tempMaxC(), day.precipitationMm())));
        var useModel = demandModel.isReady() && daysWithSales >= MINIMUM_HISTORY_DAYS;
        float[] predictions = null;
        if (useModel) {
            try {
                predictions = demandModel.predict(rows.stream().map(FeatureRow::features).toList());
            } catch (RuntimeException exception) {
                log.error("The demand model failed; using the baseline", exception);
                useModel = false;
            }
        }
        if (!useModel) {
            predictions = new float[rows.size()];
            for (var index = 0; index < rows.size(); index++) predictions[index] = (float) DemandFeatureBuilder.baseline(rows.get(index).features());
        }
        var points = new ArrayList<ForecastDataPoint>();
        for (var index = 0; index < rows.size(); index++) {
            var row = rows.get(index);
            points.add(new ForecastDataPoint(row.day().date(), row.dish(), (int) Math.round((double) predictions[index]), row.day().tempMaxC(),
                    row.day().precipitationMm()));
        }
        var confidence = useModel ? clamp(demandModel.info().confidenceBase() * coverage, 0.3, 0.95) : clamp(0.5 * coverage, 0.2, 0.6);
        var forecast = repository.save(DemandForecast.create(command.restaurantId(), clock.instant(),
                BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_EVEN), WeatherConditionClassifier.classify(weather.getFirst()),
                weather.getFirst().source(), useModel ? ForecastMethod.MACHINE_LEARNING : ForecastMethod.BASELINE,
                useModel ? demandModel.info().modelVersion() : "baseline-same-weekday-4w", points));
        forecast.registerGenerated(clock.instant());
        forecast.pullDomainEvents().forEach(eventPublisher::publishEvent);
        return Result.success(forecast);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
