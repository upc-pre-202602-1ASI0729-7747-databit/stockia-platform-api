package com.databit.stockia.platform.stockmanagement.domain.model.valueobjects;

/**
 * Status of a supply lot. Precedence: EXPIRED &gt; CRITICAL (no stock) &gt; LOW (at or below the minimum) &gt; AVAILABLE.
 */
public enum StockStatus {
    AVAILABLE,
    LOW,
    CRITICAL,
    EXPIRED
}
