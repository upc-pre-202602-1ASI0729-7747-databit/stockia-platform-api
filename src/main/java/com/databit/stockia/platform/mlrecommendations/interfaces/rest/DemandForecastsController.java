package com.databit.stockia.platform.mlrecommendations.interfaces.rest;

import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import com.databit.stockia.platform.shared.interfaces.rest.transform.ProblemDetails;
import com.databit.stockia.platform.mlrecommendations.application.commands.GenerateDemandForecastCommand;
import com.databit.stockia.platform.mlrecommendations.application.commandservices.DemandForecastCommandService;
import com.databit.stockia.platform.mlrecommendations.application.internal.commandservices.DemandForecastCommandServiceImpl;
import com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices.DemandModel;
import com.databit.stockia.platform.mlrecommendations.application.queries.GetDemandForecastsByRestaurantIdQuery;
import com.databit.stockia.platform.mlrecommendations.application.queries.GetLatestDemandForecastQuery;
import com.databit.stockia.platform.mlrecommendations.application.queryservices.DemandForecastQueryService;
import com.databit.stockia.platform.mlrecommendations.domain.services.DemandFeatureBuilder;
import com.databit.stockia.platform.mlrecommendations.interfaces.rest.resources.DemandForecastResource;
import com.databit.stockia.platform.mlrecommendations.interfaces.rest.resources.DemandModelResource;
import com.databit.stockia.platform.mlrecommendations.interfaces.rest.resources.GenerateDemandForecastResource;
import com.databit.stockia.platform.mlrecommendations.interfaces.rest.transform.DemandForecastResourceMapper;
import com.databit.stockia.platform.mlrecommendations.interfaces.rest.transform.MlRecommendationsHttpResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Demand forecast with Machine Learning (C4: Forecast Generation Service).
 */
@RestController
@RequestMapping(value = "/api/v1/demand-forecasts", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Demand Forecasts", description = "Demand forecast with Machine Learning")
public class DemandForecastsController {
    private final DemandForecastCommandService commandService;
    private final DemandForecastQueryService queryService;
    private final DemandModel demandModel;
    private final CurrentUserProvider currentUser;
    private final MessageSource messageSource;

    public DemandForecastsController(DemandForecastCommandService commandService, DemandForecastQueryService queryService, DemandModel demandModel,
                                     CurrentUserProvider currentUser, MessageSource messageSource) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.demandModel = demandModel;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List the latest forecasts", description = "Newest first; limit 1-100 (20 by default)")
    @GetMapping
    public List<DemandForecastResource> getAllDemandForecasts(@RequestParam(defaultValue = "20") int limit) {
        return queryService.handle(new GetDemandForecastsByRestaurantIdQuery(currentUser.requireRestaurantId(), limit)).stream()
                .map(DemandForecastResourceMapper::toResourceFromEntity).toList();
    }

    @Operation(summary = "Get the latest forecast")
    @GetMapping("latest")
    public ResponseEntity<?> getLatestDemandForecast() {
        return queryService.handle(new GetLatestDemandForecastQuery(currentUser.requireRestaurantId()))
                .<ResponseEntity<?>>map(forecast -> ResponseEntity.ok(DemandForecastResourceMapper.toResourceFromEntity(forecast)))
                .orElseGet(() -> ProblemDetails.response(HttpStatus.NOT_FOUND, "ForecastNotFound",
                        messageSource.getMessage("ml.error.ForecastNotFound", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Generate a forecast", description = "Forecast for today and the next days with the ONNX model, the sales history "
            + "and the Open-Meteo weather forecast. It also refreshes the purchase and menu recommendations")
    @PostMapping
    public ResponseEntity<?> generateDemandForecast(@RequestBody(required = false) GenerateDemandForecastResource resource) {
        var horizon = resource == null || resource.horizonDays() == null ? 7 : resource.horizonDays();
        var result = commandService.handle(new GenerateDemandForecastCommand(currentUser.requireRestaurantId(), horizon));
        return MlRecommendationsHttpResponseMapper.toResponseEntity(result, messageSource, forecast -> ResponseEntity
                .created(URI.create("/api/v1/demand-forecasts/latest")).body(DemandForecastResourceMapper.toResourceFromEntity(forecast)));
    }

    @Operation(summary = "Describe the demand model", description = "Version, metrics and data sources of the model in use")
    @GetMapping("model")
    public DemandModelResource getDemandModel() {
        return DemandForecastResourceMapper.toResourceFromValue(demandModel.info(), DemandFeatureBuilder.FEATURE_ORDER,
                DemandForecastCommandServiceImpl.MINIMUM_HISTORY_DAYS);
    }
}
