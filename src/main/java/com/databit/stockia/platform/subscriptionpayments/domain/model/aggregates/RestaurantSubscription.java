package com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates;

import com.databit.stockia.platform.subscriptionpayments.domain.model.valueobjects.PaymentMethod;
import com.databit.stockia.platform.subscriptionpayments.domain.model.valueobjects.SubscriptionStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * The plan a restaurant is subscribed to ("Subscription" in the web application).
 */
public class RestaurantSubscription {
    /**
     * Days covered by one monthly payment.
     */
    public static final int BILLING_PERIOD_DAYS = 30;

    private Long id;
    private final Long restaurantId;
    private Long planId;
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
    private LocalDate renewalDate;
    private PaymentMethod paymentMethod = PaymentMethod.NONE;

    private RestaurantSubscription(Long id, Long restaurantId, Long planId) {
        this.id = id;
        this.restaurantId = Objects.requireNonNull(restaurantId);
        this.planId = Objects.requireNonNull(planId);
    }

    public static RestaurantSubscription create(Long restaurantId, Long planId) {
        return new RestaurantSubscription(null, restaurantId, planId);
    }

    public static RestaurantSubscription rehydrate(Long id, Long restaurantId, Long planId, SubscriptionStatus status, LocalDate renewalDate,
                                                   PaymentMethod paymentMethod) {
        var subscription = new RestaurantSubscription(id, restaurantId, planId);
        subscription.status = status;
        subscription.renewalDate = renewalDate;
        subscription.paymentMethod = paymentMethod;
        return subscription;
    }

    /**
     * Activates a plan paid today; it renews after one billing period.
     */
    public RestaurantSubscription activate(Long planId, PaymentMethod paymentMethod, LocalDate today) {
        this.planId = planId;
        this.paymentMethod = paymentMethod;
        this.status = SubscriptionStatus.ACTIVE;
        this.renewalDate = today.plusDays(BILLING_PERIOD_DAYS);
        return this;
    }

    public Long getId() { return id; }
    public Long getRestaurantId() { return restaurantId; }
    public Long getPlanId() { return planId; }
    public SubscriptionStatus getStatus() { return status; }
    public LocalDate getRenewalDate() { return renewalDate; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
}
