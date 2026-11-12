package com.databit.stockia.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordResource(@NotBlank(message = "{validation.required}") String currentPassword,
                                     @NotBlank(message = "{validation.required}") @Size(min = 8, max = 72, message = "{validation.passwordLength}")
                                     @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "{validation.passwordStrength}") String newPassword) {
}
