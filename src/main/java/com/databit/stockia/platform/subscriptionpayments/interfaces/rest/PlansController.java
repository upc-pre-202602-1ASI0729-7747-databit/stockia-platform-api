package com.databit.stockia.platform.subscriptionpayments.interfaces.rest;

import com.databit.stockia.platform.subscriptionpayments.application.queries.GetAllPlansQuery;
import com.databit.stockia.platform.subscriptionpayments.application.queryservices.SubscriptionQueryService;
import com.databit.stockia.platform.subscriptionpayments.interfaces.rest.resources.PlanResource;
import com.databit.stockia.platform.subscriptionpayments.interfaces.rest.transform.SubscriptionResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Plan catalog (public: the landing page can show it).
 */
@RestController
@RequestMapping(value = "/api/v1/plans", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Plans", description = "Plan catalog (public)")
public class PlansController {
    private final SubscriptionQueryService subscriptionQueryService;
    private final MessageSource messageSource;

    public PlansController(SubscriptionQueryService subscriptionQueryService, MessageSource messageSource) {
        this.subscriptionQueryService = subscriptionQueryService;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List the plans", description = "Cheapest first; features in the language of Accept-Language")
    @GetMapping
    public List<PlanResource> getAllPlans() {
        var locale = LocaleContextHolder.getLocale();
        return subscriptionQueryService.handle(new GetAllPlansQuery()).stream()
                .map(plan -> SubscriptionResourceMapper.toResourceFromEntity(plan, messageSource, locale)).toList();
    }
}
