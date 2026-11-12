package com.databit.stockia.platform.notificationsmessaging.domain;

import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.*;
import com.databit.stockia.platform.notificationsmessaging.domain.services.InventoryAlertPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryAlertPolicyTest {
    private static StockSnapshot snapshot(String quantity, String min, long daysToExpire, String status) {
        return new StockSnapshot(1L, "Tomato", "kg", new BigDecimal(quantity), new BigDecimal(min), daysToExpire, status);
    }

    @Test
    void anExpiredLotWithStockRaisesACriticalAlert() {
        var proposals = InventoryAlertPolicy.evaluate(snapshot("3", "1", -2, "EXPIRED"));
        assertThat(proposals).hasSize(1);
        assertThat(proposals.getFirst().severity()).isEqualTo(AlertSeverity.CRITICAL);
        assertThat(proposals.getFirst().messageArgs()).containsExactly("Tomato", "3", "kg");
    }

    @Test
    void noStockIsCriticalAndLowStockIsAWarning() {
        assertThat(InventoryAlertPolicy.evaluate(snapshot("0", "5", 20, "CRITICAL")).getFirst().type()).isEqualTo(AlertType.CRITICAL_STOCK);
        var low = InventoryAlertPolicy.evaluate(snapshot("4.5", "6", 20, "LOW"));
        assertThat(low).extracting(AlertProposal::type).containsExactly(AlertType.LOW_STOCK);
        assertThat(low.getFirst().messageArgs()).containsExactly("Tomato", "4.5", "6", "kg");
    }

    @Test
    void aLotExpiringSoonRaisesAnEmailWarning() {
        var proposals = InventoryAlertPolicy.evaluate(snapshot("9", "8", 1, "AVAILABLE"));
        assertThat(proposals).extracting(AlertProposal::type).containsExactly(AlertType.EXPIRING_SOON);
        assertThat(proposals.getFirst().channel()).isEqualTo(AlertChannel.EMAIL);
        assertThat(proposals.getFirst().messageKey()).isEqualTo("alert.message.expiresTomorrow");
    }

    @Test
    void criticalAlertsNeedWhatsAppAndEmailBeforeTheyAreAttended() {
        var alert = Alert.createAutomatic(1L, 1L, AlertType.CRITICAL_STOCK, AlertSeverity.CRITICAL, "alert.message.outOfStock", List.of("Tomato"),
                AlertChannel.WHATSAPP, Instant.now());
        alert.registerDelivery(AlertChannel.WHATSAPP);
        assertThat(alert.pendingChannel()).contains(AlertChannel.EMAIL);
        assertThat(alert.acknowledge()).isFalse();
        alert.registerDelivery(AlertChannel.EMAIL);
        assertThat(alert.acknowledge()).isTrue();
    }
}
