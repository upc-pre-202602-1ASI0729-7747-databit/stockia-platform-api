package com.databit.stockia.platform.notificationsmessaging.application.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.notificationsmessaging.application.commands.*;
import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;

public interface AlertCommandService {
    Result<Alert, CommandFailure> handle(CreateAlertCommand command);

    Result<Alert, CommandFailure> handle(UpdateAlertCommand command);

    Result<Void, CommandFailure> handle(DeleteAlertCommand command);

    Result<Alert, CommandFailure> handle(AcknowledgeAlertCommand command);

    Result<Alert, CommandFailure> handle(RetryAlertDeliveryCommand command);

    /**
     * @return how many new alerts were created
     */
    int handle(EvaluateInventoryAlertsCommand command);
}