package com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * A sale. Channel: POS or MANUAL. Status: CONFIRMED or VOIDED.
 */
public record SaleResource(Long id, Instant saleDate, String channel, String status, List<SaleLineItemResource> lineItems, BigDecimal total) {
}
