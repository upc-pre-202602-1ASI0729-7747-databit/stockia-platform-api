package com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Same rules as the web application form: password with 8+ characters, letters and numbers.
 */
public record RegisterRestaurantResource(@NotBlank(message = "{validation.required}") @Size(min = 3, max = 120, message = "{validation.length}") String fullName,
                                         @NotBlank(message = "{validation.required}") @Size(min = 2, max = 150, message = "{validation.length}") String restaurantName,
                                         @NotBlank(message = "{validation.required}") @Email(message = "{validation.email}") @Size(max = 255, message = "{validation.maxLength}") String email,
                                         @NotBlank(message = "{validation.required}") @Size(min = 8, max = 72, message = "{validation.passwordLength}")
                                         @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "{validation.passwordStrength}") String password) {
}
