package com.databit.stockia.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInResource(@NotBlank(message = "{validation.required}") @Email(message = "{validation.email}") String email,
                             @NotBlank(message = "{validation.required}") String password) {
}
