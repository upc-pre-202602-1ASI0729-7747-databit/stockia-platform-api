package com.databit.stockia.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * temporaryPassword is optional; the configured invitation password is used when it is missing.
 */
public record InviteMemberResource(@NotBlank(message = "{validation.required}") @Size(min = 3, max = 120, message = "{validation.length}") String fullName,
                                   @NotBlank(message = "{validation.required}") @Email(message = "{validation.email}") @Size(max = 255, message = "{validation.maxLength}") String email,
                                   @NotBlank(message = "{validation.required}") String role,
                                   @Size(min = 8, max = 72, message = "{validation.passwordLength}") String temporaryPassword) {
}
