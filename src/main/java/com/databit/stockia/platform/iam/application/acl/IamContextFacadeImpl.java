package com.databit.stockia.platform.iam.application.acl;

import com.databit.stockia.platform.iam.application.commands.CreateOwnerAccountCommand;
import com.databit.stockia.platform.iam.application.commandservices.UserCommandService;
import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.domain.repositories.UserRepository;
import com.databit.stockia.platform.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IamContextFacadeImpl implements IamContextFacade {
    private final UserRepository userRepository;
    private final UserCommandService userCommandService;

    public IamContextFacadeImpl(UserRepository userRepository, UserCommandService userCommandService) {
        this.userRepository = userRepository;
        this.userCommandService = userCommandService;
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(User.normalizeEmail(email));
    }

    @Override
    public Optional<IamAccountInfo> createOwnerAccount(String fullName, String email, String password, Long restaurantId) {
        return userCommandService.handle(new CreateOwnerAccountCommand(fullName, email, password, restaurantId)).success()
                .map(session -> new IamAccountInfo(session.user().getId(), session.user().getFullName(), session.user().getEmail(),
                        session.user().getRole().name(), session.token()));
    }
}
