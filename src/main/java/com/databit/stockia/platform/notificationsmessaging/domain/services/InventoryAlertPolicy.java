package com.databit.stockia.platform.notificationsmessaging.domain.services;

import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertChannel;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertProposal;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertSeverity;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertType;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.StockSnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Which automatic alerts a supply lot needs right now: expired → EXPIRING_SOON (critical); no stock →
 * CRITICAL_STOCK (critical); at or below the minimum → LOW_STOCK (warning); expires in 0-3 days →
 * EXPIRING_SOON (warning). Critical alerts go by WhatsApp and email.
 */
public final class InventoryAlertPolicy {
    public static final int EXPIRING_SOON_DAYS = 3;

    private InventoryAlertPolicy() {
    }

    public static List<AlertProposal> evaluate(StockSnapshot item) {
        var proposals = new ArrayList<AlertProposal>();
        var hasStock = item.quantity().signum() > 0;
        if ("EXPIRED".equals(item.status())) {
            if (hasStock) proposals.add(new AlertProposal(AlertType.EXPIRING_SOON, AlertSeverity.CRITICAL, AlertChannel.WHATSAPP,
                    "alert.message.expired", List.of(item.name(), format(item.quantity()), item.unit())));
            return proposals;
        }
        if (!hasStock)
            proposals.add(new AlertProposal(AlertType.CRITICAL_STOCK, AlertSeverity.CRITICAL, AlertChannel.WHATSAPP,
                    "alert.message.outOfStock", List.of(item.name())));
        else if (item.quantity().compareTo(item.minThreshold()) <= 0)
            proposals.add(new AlertProposal(AlertType.LOW_STOCK, AlertSeverity.WARNING, AlertChannel.WHATSAPP, "alert.message.belowMinimum",
                    List.of(item.name(), format(item.quantity()), format(item.minThreshold()), item.unit())));
        if (hasStock && item.daysToExpire() >= 0 && item.daysToExpire() <= EXPIRING_SOON_DAYS) {
            var key = item.daysToExpire() == 0 ? "alert.message.expiresToday"
                    : item.daysToExpire() == 1 ? "alert.message.expiresTomorrow" : "alert.message.expiresInDays";
            proposals.add(new AlertProposal(AlertType.EXPIRING_SOON, AlertSeverity.WARNING, AlertChannel.EMAIL, key,
                    List.of(item.name(), String.valueOf(item.daysToExpire()))));
        }
        return proposals;
    }

    static String format(BigDecimal value) {
        var scaled = value.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
        return scaled.signum() == 0 ? "0" : scaled.toPlainString();
    }
}