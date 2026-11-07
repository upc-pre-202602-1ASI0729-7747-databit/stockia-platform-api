package com.databit.stockia.platform.notificationsmessaging.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

/**
 * Codes: type LOW_STOCK | CRITICAL_STOCK | EXPIRING_SOON | IOT_FAULT; severity INFO | WARNING |
 * CRITICAL; channel WHATSAPP | EMAIL. The message is in the language of the request.
 */
public record AlertResource(Long id, String type, String severity, String message, Instant createdAt, boolean acknowledged, String channel,
                            List<String> deliveredChannels, List<String> requiredChannels, boolean delivered, Long inventoryItemId) {
}