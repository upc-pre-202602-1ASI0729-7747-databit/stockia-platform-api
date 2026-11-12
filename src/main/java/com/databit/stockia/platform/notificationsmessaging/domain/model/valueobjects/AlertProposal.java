package com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects;


import java.util.List;

public record AlertProposal(AlertType type, AlertSeverity severity, AlertChannel channel, String messageKey,
                            List<String> messageArgs) {
}
