package com.databit.stockia.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record ChangeUserRoleResource(@NotBlank(message = "{validation.required}") String role) {
}
