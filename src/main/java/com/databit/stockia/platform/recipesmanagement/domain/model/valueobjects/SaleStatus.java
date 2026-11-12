package com.databit.stockia.platform.recipesmanagement.domain.model.valueobjects;

/**
 * A confirmed sale is immutable; it can only be voided.
 */
public enum SaleStatus {
    CONFIRMED,
    VOIDED
}
