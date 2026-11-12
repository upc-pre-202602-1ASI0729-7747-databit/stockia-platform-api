package com.databit.stockia.platform.mlrecommendations.application.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.mlrecommendations.application.commands.GenerateDemandForecastCommand;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.DemandForecast;

public interface DemandForecastCommandService {
    Result<DemandForecast, CommandFailure> handle(GenerateDemandForecastCommand command);
}
