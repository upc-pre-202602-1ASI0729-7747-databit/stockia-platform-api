package com.databit.stockia.platform.recipesmanagement.application.internal.queryservices;

import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.recipesmanagement.application.queries.GetSaleByIdQuery;
import com.databit.stockia.platform.recipesmanagement.application.queries.GetSalesByRestaurantIdQuery;
import com.databit.stockia.platform.recipesmanagement.application.queryservices.SaleQueryService;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Sale;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SaleQueryServiceImpl implements SaleQueryService {
    private final SaleRepository saleRepository;
    private final RestaurantClock clock;

    public SaleQueryServiceImpl(SaleRepository saleRepository, RestaurantClock clock) {
        this.saleRepository = saleRepository;
        this.clock = clock;
    }

    /**
     * Sales newest first; from/to are local dates of the restaurant (inclusive).
     */
    @Override
    @Transactional(readOnly = true)
    public List<Sale> handle(GetSalesByRestaurantIdQuery query) {
        var from = query.from() == null ? null : clock.startOf(query.from());
        var to = query.to() == null ? null : clock.endOf(query.to());
        return saleRepository.findAllByRestaurantId(query.restaurantId(), from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sale> handle(GetSaleByIdQuery query) {
        return saleRepository.findById(query.saleId()).filter(sale -> sale.getRestaurantId().equals(query.restaurantId()));
    }
}
