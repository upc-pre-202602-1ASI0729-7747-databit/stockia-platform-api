package com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.Recommendation;
import com.databit.stockia.platform.mlrecommendations.domain.repositories.RecommendationRepository;
import com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.mappers.MlPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RecommendationRepositoryAdapter implements RecommendationRepository {
    private final com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.RecommendationRepository jpa;
    private final MlPersistenceMapper mapper;

    public RecommendationRepositoryAdapter(com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.RecommendationRepository jpa, MlPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Recommendation save(Recommendation recommendation) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(recommendation)));
    }

    @Override
    public Optional<Recommendation> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Recommendation> findAllByRestaurantId(Long restaurantId) {
        return jpa.findAllByRestaurantIdOrderByCreatedAtDescIdDesc(restaurantId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Recommendation> findPendingByRestaurantId(Long restaurantId) {
        return jpa.findAllByRestaurantIdAndAppliedFalse(restaurantId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void delete(Recommendation recommendation) {
        jpa.deleteById(recommendation.getId());
    }
}
