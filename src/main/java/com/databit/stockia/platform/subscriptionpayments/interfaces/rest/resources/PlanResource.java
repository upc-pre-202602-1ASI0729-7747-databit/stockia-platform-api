package com.databit.stockia.platform.subscriptionpayments.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

/**
 * A plan; monthlyPrice in soles (PEN) and features translated to the request language.
 */
public record PlanResource(Long id, String name, BigDecimal monthlyPrice, List<String> features, boolean highlighted) {
}
