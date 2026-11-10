package com.databit.stockia.platform.subscriptionpayments.application.commandservices;

import com.databit.stockia.platform.subscriptionpayments.application.commands.SeedPlansCommand;

public interface PlanCommandService {
    void handle(SeedPlansCommand command);
}
