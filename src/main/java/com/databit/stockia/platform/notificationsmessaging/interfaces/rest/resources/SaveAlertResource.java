package com.databit.stockia.platform.notificationsmessaging.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveAlertResource(@NotBlank(message = "{validation.required}") String type,
                                @NotBlank(message = "{validation.required}") String severity,
                                @NotBlank(message = "{validation.required}") @Size(min = 5, max = 500, message = "{validation.length}") String message,
                                @NotBlank(message = "{validation.required}") String channel) {
}