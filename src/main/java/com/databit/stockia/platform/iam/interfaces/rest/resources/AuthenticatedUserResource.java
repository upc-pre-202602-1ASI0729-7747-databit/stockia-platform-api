package com.databit.stockia.platform.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(Long id, String fullName, String email, String role, Long restaurantId, String restaurantName,
                                        String restaurantAddress, String restaurantPhone, String token) {
}
