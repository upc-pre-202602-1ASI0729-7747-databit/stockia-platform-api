package com.databit.stockia.platform.shared.domain;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuantityTest {
    @Test
    void parsesDotCommaAndExponentAndKeepsExactDecimals() {
        assertThat(Quantity.parse("0,125")).isEqualTo(Quantity.parse("0.125"));
        assertThat(Quantity.parse("1.5e-3").toString()).isEqualTo("0.0015");
        assertThat(Quantity.parse("12.500").toString()).isEqualTo("12.5");
        assertThat(Quantity.parse("0.1").plus(Quantity.parse("0.2"))).isEqualTo(Quantity.parse("0.3"));
    }

    @Test
    void acceptsThirtyTwoDecimalsAndRejectsMore() {
        var thirtyTwo = "0." + "1".repeat(32);
        assertThat(Quantity.parse(thirtyTwo).toString()).isEqualTo(thirtyTwo);
        assertThatThrownBy(() -> Quantity.parse("0." + "1".repeat(33))).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity.error.tooManyDecimals");
    }

    @Test
    void rejectsTextThatIsNotANumber() {
        assertThatThrownBy(() -> Quantity.parse("abc")).hasMessage("quantity.error.invalid");
        assertThatThrownBy(() -> Quantity.parse(" ")).hasMessage("quantity.error.empty");
    }

    @Test
    void multipliesAndCompares() {
        var portion = Quantity.parse("0.18");
        assertThat(portion.times(3)).isEqualTo(new Quantity(new BigDecimal("0.54")));
        assertThat(portion.isLessThan(Quantity.of(1))).isTrue();
        assertThat(Quantity.min(portion, Quantity.of(1))).isEqualTo(portion);
    }
}
