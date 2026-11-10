package com.databit.stockia.platform.recipesmanagement.interfaces.rest;

import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import com.databit.stockia.platform.shared.interfaces.rest.transform.ProblemDetails;
import com.databit.stockia.platform.recipesmanagement.application.commands.VoidSaleCommand;
import com.databit.stockia.platform.recipesmanagement.application.commandservices.SaleCommandService;
import com.databit.stockia.platform.recipesmanagement.application.queries.GetSaleByIdQuery;
import com.databit.stockia.platform.recipesmanagement.application.queries.GetSalesByRestaurantIdQuery;
import com.databit.stockia.platform.recipesmanagement.application.queryservices.SaleQueryService;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources.RegisterSaleResource;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources.SaleResource;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.transform.RecipesManagementHttpResponseMapper;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.transform.SaleResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Sales history and sale registration (C4: Ingredient Deduction Engine).
 */
@RestController
@RequestMapping(value = "/api/v1/sales", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Sales", description = "Sales history and sale registration")
public class SalesController {
    private final SaleCommandService saleCommandService;
    private final SaleQueryService saleQueryService;
    private final CurrentUserProvider currentUser;
    private final MessageSource messageSource;

    public SalesController(SaleCommandService saleCommandService, SaleQueryService saleQueryService, CurrentUserProvider currentUser,
                           MessageSource messageSource) {
        this.saleCommandService = saleCommandService;
        this.saleQueryService = saleQueryService;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List the sales", description = "Newest first. Optional from/to (yyyy-MM-dd, restaurant time zone)")
    @GetMapping
    public List<SaleResource> getAllSales(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return saleQueryService.handle(new GetSalesByRestaurantIdQuery(currentUser.requireRestaurantId(), from, to)).stream()
                .map(SaleResourceMapper::toResourceFromEntity).toList();
    }

    @Operation(summary = "Get a sale")
    @GetMapping("{id}")
    public ResponseEntity<?> getSaleById(@PathVariable Long id) {
        return saleQueryService.handle(new GetSaleByIdQuery(id, currentUser.requireRestaurantId()))
                .<ResponseEntity<?>>map(sale -> ResponseEntity.ok(SaleResourceMapper.toResourceFromEntity(sale)))
                .orElseGet(() -> ProblemDetails.response(HttpStatus.NOT_FOUND, "SaleNotFound",
                        messageSource.getMessage("recipe.error.SaleNotFound", null, LocaleContextHolder.getLocale())));
    }

    @Operation(summary = "Register a sale", description = "422 CannotSell (nothing is recorded and the stock does not change) when a "
            + "supply is missing, short or only available in expired lots; details.shortages explains each one")
    @PostMapping
    public ResponseEntity<?> registerSale(@Valid @RequestBody RegisterSaleResource resource) {
        var result = saleCommandService.handle(SaleResourceMapper.toCommandFromResource(resource, currentUser.requireRestaurantId()));
        return RecipesManagementHttpResponseMapper.toResponseEntity(result, messageSource, sale -> ResponseEntity
                .created(URI.create("/api/v1/sales/" + sale.getId())).body(SaleResourceMapper.toResourceFromEntity(sale)));
    }

    @Operation(summary = "Void a sale", description = "The supplies used are not returned to stock")
    @PostMapping("{id}/void")
    public ResponseEntity<?> voidSale(@PathVariable Long id) {
        var result = saleCommandService.handle(new VoidSaleCommand(id, currentUser.requireRestaurantId()));
        return RecipesManagementHttpResponseMapper.toResponseEntity(result, messageSource,
                sale -> ResponseEntity.ok(SaleResourceMapper.toResourceFromEntity(sale)));
    }
}
