package com.databit.stockia.platform.restaurantregistration.application.internal.outboundservices.acl;

import com.databit.stockia.platform.iam.interfaces.acl.IamContextFacade;
import com.databit.stockia.platform.restaurantregistration.domain.model.valueobjects.RestaurantOwnerAccount;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Anti-corruption layer towards Identity &amp; Access Management.
 */
@Service
public class ExternalIamService {
    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public boolean emailExists(String email) {
        return iamContextFacade.emailExists(email);
    }

    /**
     * Creates the owner as the first Administrator; empty when the email is already registered.
     */
    public Optional<RestaurantOwnerAccount> createOwnerAccount(String fullName, String email, String password, Long restaurantId) {
        return iamContextFacade.createOwnerAccount(fullName, email, password, restaurantId)
                .map(account -> new RestaurantOwnerAccount(account.userId(), account.fullName(), account.email(), account.role(), account.token()));
    }
}
