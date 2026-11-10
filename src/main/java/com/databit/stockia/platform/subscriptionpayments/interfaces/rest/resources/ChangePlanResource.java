package com.databit.stockia.platform.subscriptionpayments.interfaces.rest.resources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * paymentMethod: STRIPE or PAYPAL (NONE only for the free plan).
 */
public record ChangePlanResource(@NotNull(message = "{validation.required}") @Min(value = 1, message = "{validation.min}") Long planId,
                                 @NotBlank(message = "{validation.required}") String paymentMethod) {
}
