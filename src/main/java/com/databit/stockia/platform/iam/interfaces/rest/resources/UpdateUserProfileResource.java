package com.databit.stockia.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileResource(@NotBlank(message = "{validation.required}") @Size(min = 3, max = 120, message = "{validation.length}") String fullName,
                                        @NotBlank(message = "{validation.required}") @Email(message = "{validation.email}") @Size(max = 255, message = "{validation.maxLength}") String email) {
}
