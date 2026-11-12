package com.databit.stockia.platform.recipesmanagement.application.queryservices;

import com.databit.stockia.platform.recipesmanagement.application.queries.GetSaleByIdQuery;
import com.databit.stockia.platform.recipesmanagement.application.queries.GetSalesByRestaurantIdQuery;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Sale;

import java.util.List;
import java.util.Optional;

public interface SaleQueryService {
    List<Sale> handle(GetSalesByRestaurantIdQuery query);

    Optional<Sale> handle(GetSaleByIdQuery query);
}
