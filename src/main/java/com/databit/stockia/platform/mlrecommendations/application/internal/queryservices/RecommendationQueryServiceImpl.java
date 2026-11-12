package com.databit.stockia.platform.mlrecommendations.application.internal.queryservices;

import com.databit.stockia.platform.mlrecommendations.application.queries.GetRecommendationsByRestaurantIdQuery;
import com.databit.stockia.platform.mlrecommendations.application.queryservices.RecommendationQueryService;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.Recommendation;
import com.databit.stockia.platform.mlrecommendations.domain.repositories.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecommendationQueryServiceImpl implements RecommendationQueryService {
    private final RecommendationRepository recommendationRepository;

    public RecommendationQueryServiceImpl(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> handle(GetRecommendationsByRestaurantIdQuery query) {
        return recommendationRepository.findAllByRestaurantId(query.restaurantId());
    }
}
