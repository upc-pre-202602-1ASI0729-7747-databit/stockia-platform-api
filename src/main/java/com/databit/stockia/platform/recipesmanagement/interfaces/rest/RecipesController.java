package com.databit.stockia.platform.recipesmanagement.interfaces.rest;

import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import com.databit.stockia.platform.shared.interfaces.rest.transform.ProblemDetails;
import com.databit.stockia.platform.recipesmanagement.application.commands.DeleteRecipeCommand;
import com.databit.stockia.platform.recipesmanagement.application.commandservices.RecipeCommandService;
import com.databit.stockia.platform.recipesmanagement.application.internal.outboundservices.ExternalStockService;
import com.databit.stockia.platform.recipesmanagement.application.queries.GetRecipeByIdQuery;
import com.databit.stockia.platform.recipesmanagement.application.queries.GetRecipesByRestaurantIdQuery;
import com.databit.stockia.platform.recipesmanagement.application.queryservices.RecipeQueryService;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources.RecipeResource;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources.SaveRecipeResource;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.transform.RecipeCommandMapper;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.transform.RecipeResourceMapper;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.transform.RecipesManagementHttpResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.IntStream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Dishes and the supplies they consume (C4: Recipe CRUD Controller).
 */
@RestController
@RequestMapping(value = "/api/v1/recipes", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Recipes", description = "Dishes and the supplies they consume")
public class RecipesController {
    private final RecipeCommandService recipeCommandService;
    private final RecipeQueryService recipeQueryService;
    private final ExternalStockService externalStockService;
    private final CurrentUserProvider currentUser;
    private final MessageSource messageSource;

    public RecipesController(RecipeCommandService recipeCommandService, RecipeQueryService recipeQueryService,
                             ExternalStockService externalStockService, CurrentUserProvider currentUser, MessageSource messageSource) {
        this.recipeCommandService = recipeCommandService;
        this.recipeQueryService = recipeQueryService;
        this.externalStockService = externalStockService;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List the recipes", description = "Recipes with the dish price, newest first")
    @GetMapping
    public List<RecipeResource> getAllRecipes() {
        var restaurantId = currentUser.requireRestaurantId();
        var recipes = recipeQueryService.handle(new GetRecipesByRestaurantIdQuery(restaurantId));
        var prices = externalStockService.pricesOf(restaurantId, recipes);
        return IntStream.range(0, recipes.size()).mapToObj(index -> RecipeResourceMapper.toResourceFromEntity(recipes.get(index), prices.get(index)))
                .toList();
    }

    @Operation(summary = "Get a recipe")
    @GetMapping("{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable Long id) {
        return recipeQueryService.handle(new GetRecipeByIdQuery(id, currentUser.requireRestaurantId()))
                .<ResponseEntity<?>>map(recipe -> ResponseEntity.ok(RecipeResourceMapper.toResourceFromEntity(recipe, externalStockService.priceOf(recipe))))
                .orElseGet(() -> ProblemDetails.response(HttpStatus.NOT_FOUND, "RecipeNotFound",
                        messageSource.getMessage("recipe.error.RecipeNotFound", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Create a recipe", description = "Each ingredient must be a supply of the inventory")
    @PostMapping
    public ResponseEntity<?> createRecipe(@Valid @RequestBody SaveRecipeResource resource) {
        var result = recipeCommandService.handle(RecipeCommandMapper.toCreateCommand(resource, currentUser.requireRestaurantId()));
        return RecipesManagementHttpResponseMapper.toResponseEntity(result, messageSource, recipe -> ResponseEntity
                .created(URI.create("/api/v1/recipes/" + recipe.getId()))
                .body(RecipeResourceMapper.toResourceFromEntity(recipe, externalStockService.priceOf(recipe))));
    }

    @Operation(summary = "Edit a recipe")
    @PutMapping("{id}")
    public ResponseEntity<?> updateRecipe(@PathVariable Long id, @Valid @RequestBody SaveRecipeResource resource) {
        var result = recipeCommandService.handle(RecipeCommandMapper.toUpdateCommand(resource, id, currentUser.requireRestaurantId()));
        return RecipesManagementHttpResponseMapper.toResponseEntity(result, messageSource,
                recipe -> ResponseEntity.ok(RecipeResourceMapper.toResourceFromEntity(recipe, externalStockService.priceOf(recipe))));
    }

    @Operation(summary = "Delete a recipe")
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        var result = recipeCommandService.handle(new DeleteRecipeCommand(id, currentUser.requireRestaurantId()));
        return RecipesManagementHttpResponseMapper.toResponseEntity(result, messageSource, ignored -> ResponseEntity.noContent().build());
    }
}
