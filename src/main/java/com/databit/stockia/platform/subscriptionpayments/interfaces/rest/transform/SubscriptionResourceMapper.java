package com.databit.stockia.platform.subscriptionpayments.interfaces.rest.transform;

import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.Plan;
import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.RestaurantSubscription;
import com.databit.stockia.platform.subscriptionpayments.interfaces.rest.resources.PlanResource;
import com.databit.stockia.platform.subscriptionpayments.interfaces.rest.resources.SubscriptionResource;
import org.springframework.context.MessageSource;

import java.util.Locale;

public final class SubscriptionResourceMapper {
    private SubscriptionResourceMapper() {
    }

    public static PlanResource toResourceFromEntity(Plan plan, MessageSource messageSource, Locale locale) {
        return new PlanResource(plan.getId(), plan.getName(), plan.getMonthlyPrice(),
                plan.getFeatures().stream().map(feature -> messageSource.getMessage(feature, null, feature, locale)).toList(), plan.isHighlighted());
    }

    public static SubscriptionResource toResourceFromEntity(RestaurantSubscription subscription) {
        return new SubscriptionResource(subscription.getId(), subscription.getPlanId(), subscription.getStatus().name(),
                subscription.getRenewalDate() == null ? null : subscription.getRenewalDate().toString(), subscription.getPaymentMethod().name());
    }
}
