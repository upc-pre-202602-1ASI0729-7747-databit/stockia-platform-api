package com.databit.stockia.platform.stockmanagement.application.commands;

public record DeleteInventoryItemCommand(Long inventoryItemId, Long restaurantId) {
}
