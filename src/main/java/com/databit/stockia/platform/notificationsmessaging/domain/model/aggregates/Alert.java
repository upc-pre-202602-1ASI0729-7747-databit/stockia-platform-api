package com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates;

import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertChannel;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertSeverity;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An alert of the restaurant. It counts as delivered only after a successful attempt on every
 * required channel: CRITICAL alerts need WhatsApp AND email, the rest only their main channel. It can
 * be marked as attended only once it was delivered. Automatic alerts keep a message key and its
 * arguments, so they are shown in the language of each user; manual alerts keep the text typed.
 */
public class Alert {
    private Long id;
    private final Long restaurantId;
    private AlertType type;
    private AlertSeverity severity;
    private String message;
    private String messageKey;
    private List<String> messageArgs = List.of();
    private AlertChannel channel;
    private List<String> deliveredChannels = new ArrayList<>();
    private boolean acknowledged;
    private final Long inventoryItemId;
    private final Instant createdAt;

    private Alert(Long id, Long restaurantId, AlertType type, AlertSeverity severity, AlertChannel channel, Long inventoryItemId, Instant createdAt) {
        this.id = id;
        this.restaurantId = Objects.requireNonNull(restaurantId);
        this.type = Objects.requireNonNull(type);
        this.severity = Objects.requireNonNull(severity);
        this.channel = Objects.requireNonNull(channel);
        this.inventoryItemId = inventoryItemId;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    /**
     * A manual alert with the text the user typed.
     */
    public static Alert createManual(Long restaurantId, AlertType type, AlertSeverity severity, String message, AlertChannel channel, Instant createdAt) {
        var alert = new Alert(null, restaurantId, type, severity, channel, null, createdAt);
        alert.setMessage(message);
        return alert;
    }

    /**
     * An automatic alert raised by a supply lot.
     */
    public static Alert createAutomatic(Long restaurantId, Long inventoryItemId, AlertType type, AlertSeverity severity, String messageKey,
                                        List<String> messageArgs, AlertChannel channel, Instant createdAt) {
        var alert = new Alert(null, restaurantId, type, severity, channel, Objects.requireNonNull(inventoryItemId), createdAt);
        alert.setLocalizedMessage(messageKey, messageArgs);
        return alert;
    }

    public static Alert rehydrate(Long id, Long restaurantId, AlertType type, AlertSeverity severity, String message, String messageKey,
                                  List<String> messageArgs, AlertChannel channel, List<String> deliveredChannels, boolean acknowledged,
                                  Long inventoryItemId, Instant createdAt) {
        var alert = new Alert(id, restaurantId, type, severity, channel, inventoryItemId, createdAt);
        alert.message = message;
        alert.messageKey = messageKey;
        alert.messageArgs = messageArgs == null ? List.of() : List.copyOf(messageArgs);
        alert.deliveredChannels = new ArrayList<>(deliveredChannels == null ? List.of() : deliveredChannels);
        alert.acknowledged = acknowledged;
        return alert;
    }

    public List<AlertChannel> requiredChannels() {
        return severity == AlertSeverity.CRITICAL ? List.of(AlertChannel.WHATSAPP, AlertChannel.EMAIL) : List.of(channel);
    }

    /**
     * @return the first required channel without a successful delivery
     */
    public Optional<AlertChannel> pendingChannel() {
        return requiredChannels().stream().filter(required -> !deliveredChannels.contains(required.name())).findFirst();
    }

    public boolean isDelivered() {
        return pendingChannel().isEmpty();
    }

    public Alert update(AlertType type, AlertSeverity severity, String message, AlertChannel channel) {
        this.type = Objects.requireNonNull(type);
        this.severity = Objects.requireNonNull(severity);
        this.channel = Objects.requireNonNull(channel);
        setMessage(message);
        return this;
    }

    /**
     * Refreshes an open automatic alert with the latest numbers instead of repeating it.
     */
    public Alert refresh(AlertType type, AlertSeverity severity, String messageKey, List<String> messageArgs, AlertChannel channel) {
        this.type = Objects.requireNonNull(type);
        this.severity = Objects.requireNonNull(severity);
        this.channel = Objects.requireNonNull(channel);
        setLocalizedMessage(messageKey, messageArgs);
        return this;
    }

    /**
     * Records a successful delivery attempt on a channel.
     */
    public Alert registerDelivery(AlertChannel deliveredChannel) {
        if (!deliveredChannels.contains(deliveredChannel.name())) deliveredChannels.add(deliveredChannel.name());
        return this;
    }

    /**
     * @return false while a required channel is still pending
     */
    public boolean acknowledge() {
        if (!isDelivered()) return false;
        acknowledged = true;
        return true;
    }

    private void setMessage(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("alert.error.message.required");
        this.message = text.trim();
        this.messageKey = null;
        this.messageArgs = List.of();
    }

    private void setLocalizedMessage(String key, List<String> args) {
        this.messageKey = Objects.requireNonNull(key);
        this.messageArgs = List.copyOf(args);
        this.message = key;
    }

    public Long getId() { return id; }
    public Long getRestaurantId() { return restaurantId; }
    public AlertType getType() { return type; }
    public AlertSeverity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public String getMessageKey() { return messageKey; }
    public List<String> getMessageArgs() { return messageArgs; }
    public AlertChannel getChannel() { return channel; }
    public List<String> getDeliveredChannels() { return List.copyOf(deliveredChannels); }
    public boolean isAcknowledged() { return acknowledged; }
    public Long getInventoryItemId() { return inventoryItemId; }
    public Instant getCreatedAt() { return createdAt; }
}