package com.databit.stockia.platform.notificationsmessaging.interfaces.acl;

/**
 * What other bounded contexts may ask Notifications &amp; Messaging.
 */
public interface NotificationsMessagingContextFacade {
    int countPendingAlerts(Long restaurantId);

    /**
     * Creates the automatic alerts of every supply of a restaurant (used after seeding demo data).
     */
    void evaluateInventoryAlerts(Long restaurantId);

    /**
     * Sends a transactional email (e.g. the password recovery link of IAM).
     *
     * @return whether the provider accepted it
     */
    boolean sendEmail(Long restaurantId, String to, String subject, String body);

    /**
     * @return true while the email provider is simulated
     */
    boolean isEmailSimulated();
}