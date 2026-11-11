package com.databit.stockia.platform.restaurantregistration.interfaces.rest;

import com.databit.stockia.platform.restaurantregistration.application.commandservices.RestaurantCommandService;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources.RegisterRestaurantResource;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources.RegisteredOwnerResource;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.transform.RestaurantRegistrationHttpResponseMapper;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.transform.RestaurantResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Restaurant onboarding (C4: Restaurant Onboarding Controller): registers a restaurant with its owner
 * as the first Administrator and the free plan, and signs the owner in.
 */
@RestController
@RequestMapping(value = "/api/v1/restaurant-registrations", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Restaurant Registrations", description = "Restaurant sign-up")
public class RestaurantRegistrationsController {
    private final RestaurantCommandService restaurantCommandService;
    private final MessageSource messageSource;

    public RestaurantRegistrationsController(RestaurantCommandService restaurantCommandService, MessageSource messageSource) {
        this.restaurantCommandService = restaurantCommandService;
        this.messageSource = messageSource;
    }

    @Operation(summary = "Register a restaurant and its owner")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Restaurant registered", content = @Content(schema = @Schema(implementation = RegisteredOwnerResource.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @PostMapping
    public ResponseEntity<?> registerRestaurant(@Valid @RequestBody RegisterRestaurantResource resource) {
        var result = restaurantCommandService.handle(RestaurantResourceMapper.toCommandFromResource(resource));
        return RestaurantRegistrationHttpResponseMapper.toResponseEntity(result, messageSource, registration -> ResponseEntity
                .created(URI.create("/api/v1/restaurants/current")).body(RestaurantResourceMapper.toResourceFromResult(registration)));
    }
}
