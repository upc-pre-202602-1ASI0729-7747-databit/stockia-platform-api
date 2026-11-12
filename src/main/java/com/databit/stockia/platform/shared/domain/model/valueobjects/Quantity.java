package com.databit.stockia.platform.shared.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Exact stock quantity with up to 32 decimals (e.g. 0.125 kg of tomato).
 * <p>
 * Doubles cannot hold the 32-decimal quantities of the Web Application, so the value is a
 * {@link BigDecimal}. It is persisted as text and serialized as a JSON string; JSON numbers
 * (also in exponent form, e.g. 1e-7) are accepted on input.
 *
 * @param value the normalized value (no trailing zeros)
 */
public record Quantity(BigDecimal value) implements Comparable<Quantity> {
    public static final int MAX_DECIMALS = 32;
    public static final Quantity ZERO = new Quantity(BigDecimal.ZERO);

    public Quantity {
        Objects.requireNonNull(value, "quantity.error.empty");
        value = normalize(value);
        if (value.scale() > MAX_DECIMALS) throw new IllegalArgumentException("quantity.error.tooManyDecimals");
    }

    public static Quantity of(long value) {
        return new Quantity(BigDecimal.valueOf(value));
    }

    /**
     * Parses "12", "0.125", "0,125" or "1.5e-3".
     *
     * @param text the quantity as text
     * @return the quantity
     * @throws IllegalArgumentException when the text is not a number or has more than 32 decimals
     */
    public static Quantity parse(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("quantity.error.empty");
        try {
            return new Quantity(new BigDecimal(text.trim().replace(',', '.')));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("quantity.error.invalid");
        }
    }

    private static BigDecimal normalize(BigDecimal value) {
        var stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    public boolean isPositive() {
        return value.signum() > 0;
    }

    public boolean isNegative() {
        return value.signum() < 0;
    }

    public Quantity plus(Quantity other) {
        return new Quantity(value.add(other.value));
    }

    public Quantity minus(Quantity other) {
        return new Quantity(value.subtract(other.value));
    }

    public Quantity times(int factor) {
        return new Quantity(value.multiply(BigDecimal.valueOf(factor)));
    }

    public boolean isLessThan(Quantity other) {
        return compareTo(other) < 0;
    }

    public boolean isLessThanOrEqualTo(Quantity other) {
        return compareTo(other) <= 0;
    }

    public static Quantity min(Quantity left, Quantity right) {
        return left.compareTo(right) <= 0 ? left : right;
    }

    public static Quantity max(Quantity left, Quantity right) {
        return left.compareTo(right) >= 0 ? left : right;
    }

    public double toDouble() {
        return value.doubleValue();
    }

    @Override
    public int compareTo(Quantity other) {
        return value.compareTo(other.value);
    }

    /**
     * @return canonical text without exponent or trailing zeros, e.g. "0.125"
     */
    @Override
    public String toString() {
        return value.toPlainString();
    }
}