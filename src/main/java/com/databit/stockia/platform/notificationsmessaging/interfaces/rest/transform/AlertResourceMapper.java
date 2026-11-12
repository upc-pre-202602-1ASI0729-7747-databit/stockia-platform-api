package com.databit.stockia.platform.notificationsmessaging.interfaces.rest.transform;

import com.databit.stockia.platform.notificationsmessaging.application.internal.outboundservices.AlertMessageResolver;
import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;
import com.databit.stockia.platform.notificationsmessaging.interfaces.rest.resources.AlertResource;

import java.util.Locale;

public final class AlertResourceMapper {
    private AlertResourceMapper() {
    }

    public static AlertResource toResourceFromEntity(Alert alert, AlertMessageResolver messageResolver, Locale locale) {
        return new AlertResource(alert.getId(), alert.getType().name(), alert.getSeverity().name(), messageResolver.textOf(alert, locale),
                alert.getCreatedAt(), alert.isAcknowledged(), alert.getChannel().name(), alert.getDeliveredChannels(),
                alert.requiredChannels().stream().map(Enum::name).toList(), alert.isDelivered(), alert.getInventoryItemId());
    }
}