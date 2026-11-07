package com.databit.stockia.platform.mlrecommendations.interfaces.rest;

import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import com.databit.stockia.platform.mlrecommendations.application.commands.ApplyRecommendationCommand;
import com.databit.stockia.platform.mlrecommendations.application.commandservices.RecommendationCommandService;
import com.databit.stockia.platform.mlrecommendations.application.queries.GetRecommendationsByRestaurantIdQuery;
import com.databit.stockia.platform.mlrecommendations.application.queryservices.RecommendationQueryService;
import com.databit.stockia.platform.mlrecommendations.interfaces.rest.resources.RecommendationResource;
import com.databit.stockia.platform.mlrecommendations.interfaces.rest.transform.DemandForecastResourceMapper;
import com.databit.stockia.platform.mlrecommendations.interfaces.rest.transform.MlRecommendationsHttpResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Recommendations generated from the demand forecast. Administrator only.
 */
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(value = "/api/v1/recommendations", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Recommendations", description = "Purchase and menu recommendations (Administrator)")
public class RecommendationsController {
    private final RecommendationCommandService recommendationCommandService;
    private final RecommendationQueryService recommendationQueryService;
    private final CurrentUserProvider currentUser;
    private final MessageSource messageSource;

    public RecommendationsController(RecommendationCommandService recommendationCommandService, RecommendationQueryService recommendationQueryService,
                                     CurrentUserProvider currentUser, MessageSource messageSource) {
        this.recommendationCommandService = recommendationCommandService;
        this.recommendationQueryService = recommendationQueryService;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List the recommendations", description = "Newest first")
    @GetMapping
    public List<RecommendationResource> getAllRecommendations() {
        var locale = LocaleContextHolder.getLocale();
        return recommendationQueryService.handle(new GetRecommendationsByRestaurantIdQuery(currentUser.requireRestaurantId())).stream()
                .map(recommendation -> DemandForecastResourceMapper.toResourceFromEntity(recommendation, messageSource, locale)).toList();
    }

    @Operation(summary = "Mark a recommendation as applied")
    @PostMapping("{id}/apply")
    public ResponseEntity<?> applyRecommendation(@PathVariable Long id) {
        var result = recommendationCommandService.handle(new ApplyRecommendationCommand(id, currentUser.requireRestaurantId()));
        return MlRecommendationsHttpResponseMapper.toResponseEntity(result, messageSource, recommendation -> ResponseEntity
                .ok(DemandForecastResourceMapper.toResourceFromEntity(recommendation, messageSource, LocaleContextHolder.getLocale())));
    }
}
