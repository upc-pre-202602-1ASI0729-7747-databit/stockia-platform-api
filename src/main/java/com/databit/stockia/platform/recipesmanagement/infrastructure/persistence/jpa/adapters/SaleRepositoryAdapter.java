package com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Sale;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.SaleRepository;
import com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.mappers.SalePersistenceMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class SaleRepositoryAdapter implements SaleRepository {
    private static final Instant FAR_FUTURE = Instant.parse("9999-01-01T00:00:00Z");
    private final com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.SaleRepository jpa;
    private final SalePersistenceMapper mapper;

    public SaleRepositoryAdapter(com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.SaleRepository jpa, SalePersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Sale save(Sale sale) {
        var entity = mapper.toEntity(sale);
        if (sale.getId() != null) jpa.findById(sale.getId()).ifPresent(existing -> entity.setCreatedAt(existing.getCreatedAt()));
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<Sale> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Sale> findAllByRestaurantId(Long restaurantId, Instant from, Instant to) {
        return jpa.findAllByRestaurantIdAndSaleDateGreaterThanEqualAndSaleDateLessThanOrderBySaleDateDescIdDesc(restaurantId,
                from == null ? Instant.EPOCH : from, to == null ? FAR_FUTURE : to).stream().map(mapper::toDomain).toList();
    }
}
