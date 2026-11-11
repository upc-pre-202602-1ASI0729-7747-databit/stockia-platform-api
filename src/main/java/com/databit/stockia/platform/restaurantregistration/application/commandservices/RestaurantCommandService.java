package com.databit.stockia.platform.restaurantregistration.application.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.restaurantregistration.application.commands.RegisterRestaurantCommand;
import com.databit.stockia.platform.restaurantregistration.application.commands.UpdateRestaurantCommand;
import com.databit.stockia.platform.restaurantregistration.domain.model.aggregates.Restaurant;
import com.databit.stockia.platform.restaurantregistration.domain.model.valueobjects.RestaurantRegistrationResult;

/**
 * Restaurant Onboarding (C4) and Profile Management of the restaurant.
 */
public interface RestaurantCommandService {
    Result<RestaurantRegistrationResult, CommandFailure> handle(RegisterRestaurantCommand command);

    Result<Restaurant, CommandFailure> handle(UpdateRestaurantCommand command);
}
