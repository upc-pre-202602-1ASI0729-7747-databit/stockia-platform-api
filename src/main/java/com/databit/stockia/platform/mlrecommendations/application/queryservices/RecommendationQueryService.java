package com.databit.stockia.platform.mlrecommendations.application.queryservices;

import com.databit.stockia.platform.mlrecommendations.application.queries.GetRecommendationsByRestaurantIdQuery;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.Recommendation;

import java.util.List;

public interface RecommendationQueryService {
    List<Recommendation> handle(GetRecommendationsByRestaurantIdQuery query);
}
