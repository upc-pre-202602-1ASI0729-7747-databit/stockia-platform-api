package com.databit.stockia.platform.iam.application.internal.outboundservices.acl;

import com.databit.stockia.platform.notificationsmessaging.interfaces.acl.NotificationsMessagingContextFacade;
import org.springframework.stereotype.Service;

/**
 * Anti-corruption layer towards Notifications &amp; Messaging (the recovery email).
 */
@Service
public class ExternalNotificationService {
    private final NotificationsMessagingContextFacade notificationsMessagingContextFacade;

    public ExternalNotificationService(NotificationsMessagingContextFacade notificationsMessagingContextFacade) {
        this.notificationsMessagingContextFacade = notificationsMessagingContextFacade;
    }

    public boolean sendEmail(Long restaurantId, String to, String subject, String body) {
        return notificationsMessagingContextFacade.sendEmail(restaurantId, to, subject, body);
    }

    /**
     * @return true while the email provider is simulated (nothing really leaves the server)
     */
    public boolean isSimulated() {
        return notificationsMessagingContextFacade.isEmailSimulated();
    }
}
