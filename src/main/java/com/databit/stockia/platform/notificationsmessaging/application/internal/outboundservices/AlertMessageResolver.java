package com.databit.stockia.platform.notificationsmessaging.application.internal.outboundservices;

import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Text of an alert in a language: the typed text of a manual alert or the translated message key of
 * an automatic one.
 */
@Component
public class AlertMessageResolver {
    private final MessageSource messageSource;

    public AlertMessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String textOf(Alert alert, Locale locale) {
        if (alert.getMessageKey() == null) return alert.getMessage();
        return messageSource.getMessage(alert.getMessageKey(), alert.getMessageArgs().toArray(), alert.getMessageKey(), locale);
    }
}