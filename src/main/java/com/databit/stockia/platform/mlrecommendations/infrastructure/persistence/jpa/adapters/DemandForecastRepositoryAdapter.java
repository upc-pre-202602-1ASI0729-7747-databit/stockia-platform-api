package com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.DemandForecast;
import com.databit.stockia.platform.mlrecommendations.domain.repositories.DemandForecastRepository;
import com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.mappers.MlPersistenceMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DemandForecastRepositoryAdapter implements DemandForecastRepository {
    private final com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.DemandForecastRepository jpa;
    private final MlPersistenceMapper mapper;

    public DemandForecastRepositoryAdapter(com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.DemandForecastRepository jpa, MlPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public DemandForecast save(DemandForecast forecast) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(forecast)));
    }

    @Override
    public Optional<DemandForecast> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<DemandForecast> findRecentByRestaurantId(Long restaurantId, int limit) {
        return jpa.findAllByRestaurantIdOrderByGeneratedAtDescIdDesc(restaurantId, PageRequest.of(0, limit)).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<DemandForecast> findLatestByRestaurantId(Long restaurantId) {
        return jpa.findFirstByRestaurantIdOrderByGeneratedAtDescIdDesc(restaurantId).map(mapper::toDomain);
    }
}
