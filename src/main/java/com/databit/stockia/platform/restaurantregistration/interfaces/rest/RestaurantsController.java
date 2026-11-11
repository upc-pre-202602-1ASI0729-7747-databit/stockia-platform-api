package com.databit.stockia.platform.restaurantregistration.interfaces.rest;

import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import com.databit.stockia.platform.shared.interfaces.rest.transform.ProblemDetails;
import com.databit.stockia.platform.restaurantregistration.application.commandservices.RestaurantCommandService;
import com.databit.stockia.platform.restaurantregistration.application.queries.GetRestaurantByIdQuery;
import com.databit.stockia.platform.restaurantregistration.application.queryservices.RestaurantQueryService;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources.UpdateRestaurantResource;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.transform.RestaurantRegistrationHttpResponseMapper;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.transform.RestaurantResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Restaurant data (C4: Profile Management Component). Every member reads it; only the Administrator changes it.
 */
@RestController
@RequestMapping(value = "/api/v1/restaurants", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Restaurants", description = "Restaurant data")
public class RestaurantsController {
    private final RestaurantCommandService restaurantCommandService;
    private final RestaurantQueryService restaurantQueryService;
    private final CurrentUserProvider currentUser;
    private final MessageSource messageSource;

    public RestaurantsController(RestaurantCommandService restaurantCommandService, RestaurantQueryService restaurantQueryService,
                                 CurrentUserProvider currentUser, MessageSource messageSource) {
        this.restaurantCommandService = restaurantCommandService;
        this.restaurantQueryService = restaurantQueryService;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Operation(summary = "Get the restaurant of the signed-in user")
    @GetMapping("current")
    public ResponseEntity<?> getCurrentRestaurant() {
        return restaurantQueryService.handle(new GetRestaurantByIdQuery(currentUser.requireRestaurantId()))
                .<ResponseEntity<?>>map(restaurant -> ResponseEntity.ok(RestaurantResourceMapper.toResourceFromEntity(restaurant)))
                .orElseGet(() -> ProblemDetails.response(HttpStatus.NOT_FOUND, "RestaurantNotFound",
                        messageSource.getMessage("restaurant.error.RestaurantNotFound", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Update the restaurant data", description = "Administrator only")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("current")
    public ResponseEntity<?> updateCurrentRestaurant(@Valid @RequestBody UpdateRestaurantResource resource) {
        var result = restaurantCommandService.handle(RestaurantResourceMapper.toCommandFromResource(resource, currentUser.requireRestaurantId()));
        return RestaurantRegistrationHttpResponseMapper.toResponseEntity(result, messageSource,
                restaurant -> ResponseEntity.ok(RestaurantResourceMapper.toResourceFromEntity(restaurant)));
    }
}
