package com.databit.stockia.platform.recipesmanagement.application.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.recipesmanagement.application.commands.RegisterSaleCommand;
import com.databit.stockia.platform.recipesmanagement.application.commands.VoidSaleCommand;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Sale;

/**
 * Ingredient Deduction Engine (C4): registers a sale only when every supply is available and takes the supplies out.
 */
public interface SaleCommandService {
    Result<Sale, CommandFailure> handle(RegisterSaleCommand command);

    Result<Sale, CommandFailure> handle(VoidSaleCommand command);
}
