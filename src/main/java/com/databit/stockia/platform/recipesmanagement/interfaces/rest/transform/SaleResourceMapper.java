package com.databit.stockia.platform.recipesmanagement.interfaces.rest.transform;

import com.databit.stockia.platform.recipesmanagement.application.commands.RegisterSaleCommand;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Sale;
import com.databit.stockia.platform.recipesmanagement.domain.model.valueobjects.SaleChannel;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources.RegisterSaleResource;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources.SaleLineItemResource;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources.SaleResource;
import com.databit.stockia.platform.shared.domain.model.valueobjects.EnumCodes;

public final class SaleResourceMapper {
    private SaleResourceMapper() {
    }

    public static SaleResource toResourceFromEntity(Sale sale) {
        return new SaleResource(sale.getId(), sale.getSaleDate(), sale.getChannel().name(), sale.getStatus().name(),
                sale.getLineItems().stream().map(line -> new SaleLineItemResource(line.recipeId(), line.dishName(), line.unitPrice(),
                        line.quantity())).toList(), sale.total());
    }

    public static RegisterSaleCommand toCommandFromResource(RegisterSaleResource resource, Long restaurantId) {
        var channel = resource.channel() == null ? SaleChannel.POS : EnumCodes.parse(SaleChannel.class, resource.channel()).orElse(SaleChannel.POS);
        return new RegisterSaleCommand(restaurantId, resource.recipeId(), resource.quantity() == null ? 1 : resource.quantity(), channel);
    }
}
