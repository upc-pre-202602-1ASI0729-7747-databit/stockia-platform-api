package com.databit.stockia.platform.stockmanagement.domain.model.valueobjects;

/**
 * Why an ingredient of a recipe cannot be supplied.
 */
public enum ShortageReason {
    /** The supply is not in the inventory anymore. */
    NOT_FOUND,
    /** Only expired lots have enough stock: an expired product is never sold. */
    EXPIRED,
    /** The lots that are not expired do not reach the required quantity. */
    INSUFFICIENT
}
