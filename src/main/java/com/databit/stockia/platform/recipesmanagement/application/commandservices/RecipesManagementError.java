package com.databit.stockia.platform.recipesmanagement.application.commandservices;

import com.databit.stockia.platform.shared.application.result.ApplicationError;

/**
 * Errors of the Recipes Management bounded context (their codes are the problem titles).
 */
public enum RecipesManagementError implements ApplicationError {
    RECIPE_NOT_FOUND,
    INGREDIENT_NOT_FOUND,
    INVALID_DATA,
    CANNOT_SELL,
    SALE_NOT_FOUND,
    ALREADY_VOIDED,
    DATABASE_ERROR;

    @Override
    public String messageKeyPrefix() {
        return "recipe.error";
    }
}
