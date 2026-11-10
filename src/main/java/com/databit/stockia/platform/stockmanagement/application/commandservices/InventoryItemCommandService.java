package com.databit.stockia.platform.stockmanagement.application.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.stockmanagement.application.commands.CreateInventoryItemCommand;
import com.databit.stockia.platform.stockmanagement.application.commands.DeductStockCommand;
import com.databit.stockia.platform.stockmanagement.application.commands.DeleteInventoryItemCommand;
import com.databit.stockia.platform.stockmanagement.application.commands.UpdateInventoryItemCommand;
import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;

/**
 * Add, modify and remove supplies (C4: Add / Modify / Remove Supplies Component) and take out what a sale consumes.
 */
public interface InventoryItemCommandService {
    Result<InventoryItem, CommandFailure> handle(CreateInventoryItemCommand command);

    Result<InventoryItem, CommandFailure> handle(UpdateInventoryItemCommand command);

    Result<Void, CommandFailure> handle(DeleteInventoryItemCommand command);

    Result<Void, CommandFailure> handle(DeductStockCommand command);
}
