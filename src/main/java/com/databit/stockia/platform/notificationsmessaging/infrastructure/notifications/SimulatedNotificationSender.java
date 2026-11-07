package com.databit.stockia.platform.notificationsmessaging.infrastructure.notifications;

import com.databit.stockia.platform.notificationsmessaging.application.internal.outboundservices.NotificationSender;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Simulated WhatsApp / email delivery: always succeeds and logs the message.
 */
@Slf4j
@Component
public class SimulatedNotificationSender implements NotificationSender {
    @Override
    public boolean isSimulated() {
        return true;
    }

    @Override
    public boolean send(Long restaurantId, AlertChannel channel, String message) {
        log.info("[{}] restaurant {}: {}", channel, restaurantId, message);
        return true;
    }

    @Override
    public boolean sendEmail(Long restaurantId, String to, String subject, String body) {
        log.info("[EMAIL] restaurant {} to {}: {}\n{}", restaurantId, to, subject, body);
        return true;
    }
}