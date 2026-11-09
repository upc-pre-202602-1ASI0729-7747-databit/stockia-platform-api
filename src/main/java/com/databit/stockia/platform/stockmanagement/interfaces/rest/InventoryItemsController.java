package com.databit.stockia.platform.stockmanagement.interfaces.rest;

import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.shared.domain.model.valueobjects.EnumCodes;
import com.databit.stockia.platform.shared.interfaces.rest.transform.ProblemDetails;
import com.databit.stockia.platform.stockmanagement.application.commands.DeleteInventoryItemCommand;
import com.databit.stockia.platform.stockmanagement.application.commandservices.InventoryItemCommandService;
import com.databit.stockia.platform.stockmanagement.application.queries.GetInventoryItemByIdQuery;
import com.databit.stockia.platform.stockmanagement.application.queries.GetInventoryItemsByRestaurantIdQuery;
import com.databit.stockia.platform.stockmanagement.application.queryservices.InventoryItemQueryService;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StorageType;
import com.databit.stockia.platform.stockmanagement.interfaces.rest.resources.InventoryItemResource;
import com.databit.stockia.platform.stockmanagement.interfaces.rest.resources.SaveInventoryItemResource;
import com.databit.stockia.platform.stockmanagement.interfaces.rest.transform.InventoryItemCommandMapper;
import com.databit.stockia.platform.stockmanagement.interfaces.rest.transform.InventoryItemResourceMapper;
import com.databit.stockia.platform.stockmanagement.interfaces.rest.transform.StockManagementHttpResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Supplies (lots) of the restaurant: Add / Modify / Remove Supplies (C4) and the Inventory Aggregator view.
 */
@RestController
@RequestMapping(value = "/api/v1/inventory-items", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Inventory Items", description = "Supplies (lots) of the restaurant")
public class InventoryItemsController {
    private final InventoryItemCommandService inventoryItemCommandService;
    private final InventoryItemQueryService inventoryItemQueryService;
    private final CurrentUserProvider currentUser;
    private final RestaurantClock clock;
    private final MessageSource messageSource;

    public InventoryItemsController(InventoryItemCommandService inventoryItemCommandService, InventoryItemQueryService inventoryItemQueryService,
                                    CurrentUserProvider currentUser, RestaurantClock clock, MessageSource messageSource) {
        this.inventoryItemCommandService = inventoryItemCommandService;
        this.inventoryItemQueryService = inventoryItemQueryService;
        this.currentUser = currentUser;
        this.clock = clock;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List the supplies", description = "Supply lots of the restaurant, newest first")
    @GetMapping
    public List<InventoryItemResource> getAllInventoryItems() {
        var today = clock.today();
        return inventoryItemQueryService.handle(new GetInventoryItemsByRestaurantIdQuery(currentUser.requireRestaurantId())).stream()
                .map(item -> InventoryItemResourceMapper.toResourceFromEntity(item, today)).toList();
    }

    @Operation(summary = "Get a supply")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supply found", content = @Content(schema = @Schema(implementation = InventoryItemResource.class))),
            @ApiResponse(responseCode = "404", description = "Supply not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @GetMapping("{id}")
    public ResponseEntity<?> getInventoryItemById(@PathVariable Long id) {
        return inventoryItemQueryService.handle(new GetInventoryItemByIdQuery(id, currentUser.requireRestaurantId()))
                .<ResponseEntity<?>>map(item -> ResponseEntity.ok(InventoryItemResourceMapper.toResourceFromEntity(item, clock.today())))
                .orElseGet(() -> notFound());
    }

    @Operation(summary = "Register a supply lot", description = "The expiration date is today plus the shelf life")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supply created", content = @Content(schema = @Schema(implementation = InventoryItemResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @PostMapping
    public ResponseEntity<?> createInventoryItem(@Valid @RequestBody SaveInventoryItemResource resource) {
        var storageType = EnumCodes.parse(StorageType.class, resource.storageType());
        if (storageType.isEmpty()) return invalidStorageType();
        var result = inventoryItemCommandService.handle(
                InventoryItemCommandMapper.toCreateCommand(resource, storageType.get(), currentUser.requireRestaurantId()));
        return StockManagementHttpResponseMapper.toResponseEntity(result, messageSource, item -> ResponseEntity
                .created(URI.create("/api/v1/inventory-items/" + item.getId()))
                .body(InventoryItemResourceMapper.toResourceFromEntity(item, clock.today())));
    }

    @Operation(summary = "Edit a supply", description = "The expiration date is recalculated from the shelf life")
    @PutMapping("{id}")
    public ResponseEntity<?> updateInventoryItem(@PathVariable Long id, @Valid @RequestBody SaveInventoryItemResource resource) {
        var storageType = EnumCodes.parse(StorageType.class, resource.storageType());
        if (storageType.isEmpty()) return invalidStorageType();
        var result = inventoryItemCommandService.handle(
                InventoryItemCommandMapper.toUpdateCommand(resource, storageType.get(), id, currentUser.requireRestaurantId()));
        return StockManagementHttpResponseMapper.toResponseEntity(result, messageSource,
                item -> ResponseEntity.ok(InventoryItemResourceMapper.toResourceFromEntity(item, clock.today())));
    }

    @Operation(summary = "Delete a supply")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supply deleted"),
            @ApiResponse(responseCode = "404", description = "Supply not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteInventoryItem(@PathVariable Long id) {
        var result = inventoryItemCommandService.handle(new DeleteInventoryItemCommand(id, currentUser.requireRestaurantId()));
        return StockManagementHttpResponseMapper.toResponseEntity(result, messageSource, ignored -> ResponseEntity.noContent().build());
    }

    private ResponseEntity<?> invalidStorageType() {
        return ProblemDetails.response(HttpStatus.BAD_REQUEST, "InvalidStorageType",
                messageSource.getMessage("stock.error.InvalidStorageType", null, LocaleContextHolder.getLocale()));
    }

    private ResponseEntity<?> notFound() {
        return ProblemDetails.response(HttpStatus.NOT_FOUND, "InventoryItemNotFound",
                messageSource.getMessage("stock.error.InventoryItemNotFound", null, LocaleContextHolder.getLocale()));
    }
}
