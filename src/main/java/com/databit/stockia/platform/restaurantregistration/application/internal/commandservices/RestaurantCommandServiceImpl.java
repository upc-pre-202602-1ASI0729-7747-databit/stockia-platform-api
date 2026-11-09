package com.databit.stockia.platform.restaurantregistration.application.internal.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.LocalizedText;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.application.transaction.TransactionRollback;
import com.databit.stockia.platform.restaurantregistration.application.commands.RegisterRestaurantCommand;
import com.databit.stockia.platform.restaurantregistration.application.commands.UpdateRestaurantCommand;
import com.databit.stockia.platform.restaurantregistration.application.commandservices.RestaurantCommandService;
import com.databit.stockia.platform.restaurantregistration.application.commandservices.RestaurantRegistrationError;
import com.databit.stockia.platform.restaurantregistration.application.internal.outboundservices.acl.ExternalIamService;
import com.databit.stockia.platform.restaurantregistration.application.internal.outboundservices.acl.ExternalSubscriptionService;
import com.databit.stockia.platform.restaurantregistration.domain.model.aggregates.Restaurant;
import com.databit.stockia.platform.restaurantregistration.domain.model.valueobjects.RestaurantRegistrationResult;
import com.databit.stockia.platform.restaurantregistration.domain.repositories.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Restaurant Onboarding (C4) and Profile Management of the restaurant. StockIA serves one restaurant per account.
 */
@Slf4j
@Service
public class RestaurantCommandServiceImpl implements RestaurantCommandService {
    private final RestaurantRepository restaurantRepository;
    private final ExternalIamService externalIamService;
    private final ExternalSubscriptionService externalSubscriptionService;

    public RestaurantCommandServiceImpl(RestaurantRepository restaurantRepository, ExternalIamService externalIamService,
                                        ExternalSubscriptionService externalSubscriptionService) {
        this.restaurantRepository = restaurantRepository;
        this.externalIamService = externalIamService;
        this.externalSubscriptionService = externalSubscriptionService;
    }

    /**
     * One transaction: restaurant, owner account (IAM), owner link and free plan (Subscription &amp;
     * Payments). A failure in any step rolls back all of them; a duplicated email is rejected before
     * writing anything.
     */
    @Override
    @Transactional
    public Result<RestaurantRegistrationResult, CommandFailure> handle(RegisterRestaurantCommand command) {
        var email = command.email().trim().toLowerCase(Locale.ROOT);
        if (externalIamService.emailExists(email)) return emailTaken(email);
        Restaurant restaurant;
        try {
            restaurant = restaurantRepository.save(Restaurant.create(command.restaurantName()));
        } catch (IllegalArgumentException exception) {
            return Result.failure(CommandFailure.of(RestaurantRegistrationError.INVALID_DATA, LocalizedText.of(exception.getMessage())));
        }
        var owner = externalIamService.createOwnerAccount(command.ownerFullName(), email, command.password(), restaurant.getId());
        if (owner.isEmpty()) {
            TransactionRollback.markRollbackOnly();
            return emailTaken(email);
        }
        restaurant = restaurantRepository.save(restaurant.linkOwner(owner.get().userId()));
        try {
            externalSubscriptionService.startFreePlan(restaurant.getId());
        } catch (RuntimeException exception) {
            log.error("The free plan could not be started for restaurant {}", restaurant.getId(), exception);
            TransactionRollback.markRollbackOnly();
            return Result.failure(CommandFailure.of(RestaurantRegistrationError.DATABASE_ERROR));
        }
        return Result.success(new RestaurantRegistrationResult(restaurant, owner.get()));
    }

    /**
     * Restaurant data (Account settings). Only the Administrator reaches it (checked by the controller).
     */
    @Override
    @Transactional
    public Result<Restaurant, CommandFailure> handle(UpdateRestaurantCommand command) {
        var found = restaurantRepository.findById(command.restaurantId());
        if (found.isEmpty()) return Result.failure(CommandFailure.of(RestaurantRegistrationError.RESTAURANT_NOT_FOUND));
        try {
            return Result.success(restaurantRepository.save(found.get().updateProfile(command.name(), command.address(), command.phone())));
        } catch (IllegalArgumentException exception) {
            return Result.failure(CommandFailure.of(RestaurantRegistrationError.INVALID_DATA, LocalizedText.of(exception.getMessage())));
        }
    }

    private static Result<RestaurantRegistrationResult, CommandFailure> emailTaken(String email) {
        return Result.failure(CommandFailure.of(RestaurantRegistrationError.EMAIL_ALREADY_TAKEN, email));
    }
}
