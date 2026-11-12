package com.databit.stockia.platform.subscriptionpayments.infrastructure.payments;

import com.databit.stockia.platform.subscriptionpayments.application.internal.outboundservices.PaymentGateway;
import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.Plan;
import com.databit.stockia.platform.subscriptionpayments.domain.model.valueobjects.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Simulated checkout: always approves and logs the charge. No real money moves.
 */
@Slf4j
@Component
public class SimulatedPaymentGateway implements PaymentGateway {
    @Override
    public boolean charge(Long restaurantId, Plan plan, PaymentMethod paymentMethod) {
        log.info("Simulated {} charge of S/ {} for plan {} (restaurant {})", paymentMethod, plan.getMonthlyPrice(), plan.getName(), restaurantId);
        return true;
    }
}
