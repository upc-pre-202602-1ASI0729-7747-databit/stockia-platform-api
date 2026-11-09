package com.databit.stockia.platform.stockmanagement.application.commandservices;

import com.databit.stockia.platform.shared.application.result.ApplicationError;

/**
 * Errors of the Stock Management bounded context (their codes are the problem titles).
 */
public enum StockManagementError implements ApplicationError {
    INVENTORY_ITEM_NOT_FOUND,
    INVALID_DATA,
    INSUFFICIENT_STOCK;

    @Override
    public String messageKeyPrefix() {
        return "stock.error";
    }
}
