package com.databit.stockia.platform.subscriptionpayments.application.internal.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.subscriptionpayments.application.commands.ChangePlanCommand;
import com.databit.stockia.platform.subscriptionpayments.application.commands.CreateDefaultSubscriptionCommand;
import com.databit.stockia.platform.subscriptionpayments.application.commandservices.SubscriptionCommandService;
import com.databit.stockia.platform.subscriptionpayments.application.commandservices.SubscriptionPaymentsError;
import com.databit.stockia.platform.subscriptionpayments.application.internal.outboundservices.PaymentGateway;
import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.RestaurantSubscription;
import com.databit.stockia.platform.subscriptionpayments.domain.model.valueobjects.PaymentMethod;
import com.databit.stockia.platform.subscriptionpayments.domain.repositories.PlanRepository;
import com.databit.stockia.platform.subscriptionpayments.domain.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentGateway paymentGateway;
    private final RestaurantClock clock;

    public SubscriptionCommandServiceImpl(PlanRepository planRepository, SubscriptionRepository subscriptionRepository,
                                          PaymentGateway paymentGateway, RestaurantClock clock) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentGateway = paymentGateway;
        this.clock = clock;
    }

    /**
     * Every new restaurant starts with the free plan (called by the restaurant sign-up).
     */
    @Override
    @Transactional
    public Result<RestaurantSubscription, CommandFailure> handle(CreateDefaultSubscriptionCommand command) {
        var existing = subscriptionRepository.findByRestaurantId(command.restaurantId());
        if (existing.isPresent()) return Result.success(existing.get());
        var plan = planRepository.findFreePlan();
        if (plan.isEmpty()) return Result.failure(CommandFailure.of(SubscriptionPaymentsError.PLAN_NOT_FOUND));
        var subscription = RestaurantSubscription.create(command.restaurantId(), plan.get().getId())
                .activate(plan.get().getId(), PaymentMethod.NONE, clock.today());
        return Result.success(subscriptionRepository.save(subscription));
    }

    /**
     * Simulated checkout with Stripe or PayPal, then the plan is activated.
     */
    @Override
    @Transactional
    public Result<RestaurantSubscription, CommandFailure> handle(ChangePlanCommand command) {
        var found = planRepository.findById(command.planId());
        if (found.isEmpty()) return Result.failure(CommandFailure.of(SubscriptionPaymentsError.PLAN_NOT_FOUND));
        var plan = found.get();
        if (!plan.isFree() && command.paymentMethod() == PaymentMethod.NONE)
            return Result.failure(CommandFailure.of(SubscriptionPaymentsError.INVALID_PAYMENT_METHOD));
        if (!plan.isFree() && !paymentGateway.charge(command.restaurantId(), plan, command.paymentMethod()))
            return Result.failure(CommandFailure.of(SubscriptionPaymentsError.PAYMENT_REJECTED));
        var subscription = subscriptionRepository.findByRestaurantId(command.restaurantId())
                .orElseGet(() -> RestaurantSubscription.create(command.restaurantId(), plan.getId()));
        return Result.success(subscriptionRepository.save(subscription.activate(plan.getId(), command.paymentMethod(), clock.today())));
    }
}
