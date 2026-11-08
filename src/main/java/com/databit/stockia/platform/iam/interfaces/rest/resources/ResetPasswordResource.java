package com.databit.stockia.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Email and code come from the recovery link; same password rules as sign-up.
 */
public record ResetPasswordResource(@NotBlank(message = "{validation.required}") @Email(message = "{validation.email}") @Size(max = 255, message = "{validation.maxLength}") String email,
                                    @NotBlank(message = "{validation.required}") @Pattern(regexp = "^\\d{6}$", message = "{validation.resetCode}") String code,
                                    @NotBlank(message = "{validation.required}") @Size(min = 8, max = 72, message = "{validation.passwordLength}")
                                    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "{validation.passwordStrength}") String newPassword) {
}
