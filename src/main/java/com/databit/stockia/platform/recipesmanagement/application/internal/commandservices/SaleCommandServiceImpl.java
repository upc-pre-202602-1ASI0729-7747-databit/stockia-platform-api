package com.databit.stockia.platform.recipesmanagement.application.internal.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.shared.application.transaction.TransactionRollback;
import com.databit.stockia.platform.recipesmanagement.application.commands.RegisterSaleCommand;
import com.databit.stockia.platform.recipesmanagement.application.commands.VoidSaleCommand;
import com.databit.stockia.platform.recipesmanagement.application.commandservices.RecipesManagementError;
import com.databit.stockia.platform.recipesmanagement.application.commandservices.SaleCommandService;
import com.databit.stockia.platform.recipesmanagement.application.internal.outboundservices.ExternalStockService;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Sale;
import com.databit.stockia.platform.recipesmanagement.domain.model.entities.SaleLineItem;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.RecipeRepository;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.SaleRepository;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade.ShortageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Ingredient Deduction Engine (C4). When a dish is sold ("Dish Sold"), in one transaction: check the
 * stock with Stock Management (expired lots excluded), reject without writing anything when a supply
 * is missing, record the sale priced at the dish cost and take out the supplies.
 */
@Service
public class SaleCommandServiceImpl implements SaleCommandService {
    private final SaleRepository saleRepository;
    private final RecipeRepository recipeRepository;
    private final ExternalStockService externalStockService;
    private final RestaurantClock clock;

    public SaleCommandServiceImpl(SaleRepository saleRepository, RecipeRepository recipeRepository,
                                  ExternalStockService externalStockService, RestaurantClock clock) {
        this.saleRepository = saleRepository;
        this.recipeRepository = recipeRepository;
        this.externalStockService = externalStockService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Result<Sale, CommandFailure> handle(RegisterSaleCommand command) {
        var found = recipeRepository.findById(command.recipeId()).filter(recipe -> recipe.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty())
            return Result.failure(CommandFailure.withMessage(RecipesManagementError.RECIPE_NOT_FOUND, "recipe.error.RecipeToSellNotFound"));
        var recipe = found.get();
        var availability = externalStockService.checkAvailability(recipe, command.quantity());
        if (!availability.canFulfill()) return cannotSell(recipe.getDishName(), availability.shortages());
        var sale = saleRepository.save(Sale.create(command.restaurantId(), clock.instant(), command.channel(),
                List.of(new SaleLineItem(recipe.getId(), recipe.getDishName(), availability.unitPrice(), command.quantity()))));
        // Dish Sold: take out the supplies, re-checked inside the transaction.
        var shortages = externalStockService.deductStock(recipe, command.quantity());
        if (!shortages.isEmpty()) {
            TransactionRollback.markRollbackOnly();
            return cannotSell(recipe.getDishName(), shortages);
        }
        return Result.success(sale);
    }

    @Override
    @Transactional
    public Result<Sale, CommandFailure> handle(VoidSaleCommand command) {
        var found = saleRepository.findById(command.saleId()).filter(sale -> sale.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty()) return Result.failure(CommandFailure.of(RecipesManagementError.SALE_NOT_FOUND));
        var sale = found.get();
        if (!sale.voidSale()) return Result.failure(CommandFailure.of(RecipesManagementError.ALREADY_VOIDED));
        return Result.success(saleRepository.save(sale));
    }

    private static Result<Sale, CommandFailure> cannotSell(String dishName, List<ShortageInfo> shortages) {
        return Result.failure(CommandFailure.of(RecipesManagementError.CANNOT_SELL, dishName)
                .withDetails(Map.of("dish", dishName, "shortages", shortages)));
    }
}
