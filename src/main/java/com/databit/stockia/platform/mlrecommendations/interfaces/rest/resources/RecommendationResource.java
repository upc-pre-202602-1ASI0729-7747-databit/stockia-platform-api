package com.databit.stockia.platform.mlrecommendations.interfaces.rest.resources;

import java.time.Instant;

/**
 * type: PURCHASE_SUGGESTION or MENU_ADJUSTMENT; texts in the language of the request.
 */
public record RecommendationResource(Long id, String type, String message, String expectedImpact, boolean applied, Instant createdAt,
                                     Long demandForecastId) {
}
