package com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates;

import java.math.BigDecimal;
import java.util.List;

/**
 * A plan of the catalog (Esencial, Profesional, IoT Completo). Features are message keys translated
 * for each request.
 */
public class Plan {
    private Long id;
    private final String name;
    private final BigDecimal monthlyPrice;
    private final List<String> features;
    private final boolean highlighted;

    private Plan(Long id, String name, BigDecimal monthlyPrice, List<String> features, boolean highlighted) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("plan.error.name.required");
        if (monthlyPrice == null || monthlyPrice.signum() < 0) throw new IllegalArgumentException("plan.error.price.negative");
        this.id = id;
        this.name = name;
        this.monthlyPrice = monthlyPrice;
        this.features = List.copyOf(features);
        this.highlighted = highlighted;
    }

    public static Plan create(String name, BigDecimal monthlyPrice, List<String> features, boolean highlighted) {
        return new Plan(null, name, monthlyPrice, features, highlighted);
    }

    public static Plan rehydrate(Long id, String name, BigDecimal monthlyPrice, List<String> features, boolean highlighted) {
        return new Plan(id, name, monthlyPrice, features, highlighted);
    }

    /**
     * @return true for the free plan every restaurant starts with
     */
    public boolean isFree() {
        return monthlyPrice.signum() == 0;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public List<String> getFeatures() { return features; }
    public boolean isHighlighted() { return highlighted; }
}
