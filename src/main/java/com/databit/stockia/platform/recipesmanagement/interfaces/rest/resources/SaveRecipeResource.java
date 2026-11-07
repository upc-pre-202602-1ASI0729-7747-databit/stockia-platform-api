package com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveRecipeResource(@NotBlank(message = "{validation.required}") @Size(max = 120, message = "{validation.maxLength}") String dishName,
                                 @NotEmpty(message = "{validation.required}") List<@Valid SaveRecipeIngredientResource> ingredients) {
}
