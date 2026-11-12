package com.databit.stockia.platform.subscriptionpayments.application.internal.commandservices;

import com.databit.stockia.platform.subscriptionpayments.application.commands.SeedPlansCommand;
import com.databit.stockia.platform.subscriptionpayments.application.commandservices.PlanCommandService;
import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.Plan;
import com.databit.stockia.platform.subscriptionpayments.domain.repositories.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Keeps the plan catalog: Esencial (free), Profesional (highlighted) and IoT Completo.
 */
@Service
public class PlanCommandServiceImpl implements PlanCommandService {
    private final PlanRepository planRepository;

    public PlanCommandServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional
    public void handle(SeedPlansCommand command) {
        seed("Esencial", "0", List.of("plan.feature.upTo50Supplies", "plan.feature.basicSales", "plan.feature.oneUser"), false);
        seed("Profesional", "39", List.of("plan.feature.unlimitedSupplies", "plan.feature.aiDemandForecast", "plan.feature.upTo5Users",
                "plan.feature.automaticRecommendations"), true);
        seed("IoT Completo", "79", List.of("plan.feature.everythingInProfessional", "plan.feature.iotSensors", "plan.feature.realTimeAlerts",
                "plan.feature.unlimitedUsers"), false);
    }

    private void seed(String name, String price, List<String> features, boolean highlighted) {
        if (!planRepository.existsByName(name)) planRepository.save(Plan.create(name, new BigDecimal(price), features, highlighted));
    }
}
