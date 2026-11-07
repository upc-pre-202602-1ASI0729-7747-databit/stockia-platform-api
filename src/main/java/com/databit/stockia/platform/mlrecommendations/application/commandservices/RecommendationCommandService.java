package com.databit.stockia.platform.mlrecommendations.application.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.mlrecommendations.application.commands.ApplyRecommendationCommand;
import com.databit.stockia.platform.mlrecommendations.application.commands.GenerateRecommendationsCommand;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.Recommendation;

import java.util.List;

public interface RecommendationCommandService {
    List<Recommendation> handle(GenerateRecommendationsCommand command);

    Result<Recommendation, CommandFailure> handle(ApplyRecommendationCommand command);
}
