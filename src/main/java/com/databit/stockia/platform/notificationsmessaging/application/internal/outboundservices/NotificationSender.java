package com.databit.stockia.platform.notificationsmessaging.application.internal.outboundservices;

import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertChannel;

/**
 * Sends alerts by WhatsApp or email and transactional emails. The current implementation is simulated
 * (it logs the message); Twilio (WhatsApp) and SendGrid (C4: SendGrid Adapter) only need to implement
 * this interface.
 */
public interface NotificationSender {
    /**
     * @return true when nothing really leaves the server
     */
    boolean isSimulated();

    boolean send(Long restaurantId, AlertChannel channel, String message);

    boolean sendEmail(Long restaurantId, String to, String subject, String body);
}