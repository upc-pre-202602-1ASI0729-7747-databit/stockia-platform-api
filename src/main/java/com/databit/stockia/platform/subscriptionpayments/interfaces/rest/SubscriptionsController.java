package com.databit.stockia.platform.subscriptionpayments.interfaces.rest;

import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import com.databit.stockia.platform.shared.domain.model.valueobjects.EnumCodes;
import com.databit.stockia.platform.shared.interfaces.rest.transform.ProblemDetails;
import com.databit.stockia.platform.subscriptionpayments.application.commands.ChangePlanCommand;
import com.databit.stockia.platform.subscriptionpayments.application.commandservices.SubscriptionCommandService;
import com.databit.stockia.platform.subscriptionpayments.application.queries.GetSubscriptionByRestaurantIdQuery;
import com.databit.stockia.platform.subscriptionpayments.application.queryservices.SubscriptionQueryService;
import com.databit.stockia.platform.subscriptionpayments.domain.model.valueobjects.PaymentMethod;
import com.databit.stockia.platform.subscriptionpayments.interfaces.rest.resources.ChangePlanResource;
import com.databit.stockia.platform.subscriptionpayments.interfaces.rest.transform.SubscriptionPaymentsHttpResponseMapper;
import com.databit.stockia.platform.subscriptionpayments.interfaces.rest.transform.SubscriptionResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Subscription type and upgrade plan: Administrator only.
 */
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(value = "/api/v1/subscriptions", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Subscriptions", description = "Subscription of the restaurant (Administrator)")
public class SubscriptionsController {
    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;
    private final CurrentUserProvider currentUser;
    private final MessageSource messageSource;

    public SubscriptionsController(SubscriptionCommandService subscriptionCommandService, SubscriptionQueryService subscriptionQueryService,
                                   CurrentUserProvider currentUser, MessageSource messageSource) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Operation(summary = "Get the current subscription")
    @GetMapping("current")
    public ResponseEntity<?> getCurrentSubscription() {
        return subscriptionQueryService.handle(new GetSubscriptionByRestaurantIdQuery(currentUser.requireRestaurantId()))
                .<ResponseEntity<?>>map(subscription -> ResponseEntity.ok(SubscriptionResourceMapper.toResourceFromEntity(subscription)))
                .orElseGet(() -> problem(HttpStatus.NOT_FOUND, "SubscriptionNotFound", "subscription.error.SubscriptionNotFound"));
    }

    @Operation(summary = "Choose a plan", description = "Simulated Stripe / PayPal checkout")
    @PostMapping
    public ResponseEntity<?> changePlan(@Valid @RequestBody ChangePlanResource resource) {
        var paymentMethod = EnumCodes.parse(PaymentMethod.class, resource.paymentMethod());
        if (paymentMethod.isEmpty()) return problem(HttpStatus.BAD_REQUEST, "InvalidPaymentMethod", "subscription.error.InvalidPaymentMethod");
        var result = subscriptionCommandService.handle(new ChangePlanCommand(currentUser.requireRestaurantId(), resource.planId(), paymentMethod.get()));
        return SubscriptionPaymentsHttpResponseMapper.toResponseEntity(result, messageSource,
                subscription -> ResponseEntity.ok(SubscriptionResourceMapper.toResourceFromEntity(subscription)));
    }

    private ResponseEntity<?> problem(HttpStatus status, String title, String key) {
        return ProblemDetails.response(status, title, messageSource.getMessage(key, null, LocaleContextHolder.getLocale()));
    }
}
