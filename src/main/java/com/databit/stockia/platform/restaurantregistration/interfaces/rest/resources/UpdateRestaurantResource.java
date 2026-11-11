package com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateRestaurantResource(@NotBlank(message = "{validation.required}") @Size(min = 2, max = 150, message = "{validation.length}") String name,
                                       @Size(max = 255, message = "{validation.maxLength}") String address,
                                       @Size(max = 30, message = "{validation.maxLength}") @Pattern(regexp = "^$|^\\+?[\\d\\s-]{6,20}$", message = "{validation.phone}") String phone) {
}
