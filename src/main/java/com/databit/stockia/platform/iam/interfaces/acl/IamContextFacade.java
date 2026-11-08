package com.databit.stockia.platform.iam.interfaces.acl;

import java.util.Optional;

/**
 * What other bounded contexts may ask Identity &amp; Access Management.
 */
public interface IamContextFacade {
    /**
     * A user account created through the ACL, already signed in.
     */
    record IamAccountInfo(Long userId, String fullName, String email, String role, String token) {
    }

    boolean emailExists(String email);

    /**
     * Creates the first Administrator of a new restaurant.
     *
     * @return the account, or empty when it could not be created
     */
    Optional<IamAccountInfo> createOwnerAccount(String fullName, String email, String password, Long restaurantId);
}
