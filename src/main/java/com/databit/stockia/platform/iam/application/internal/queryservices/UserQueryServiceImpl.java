package com.databit.stockia.platform.iam.application.internal.queryservices;

import com.databit.stockia.platform.iam.application.queries.GetUserByIdQuery;
import com.databit.stockia.platform.iam.application.queries.GetUsersByRestaurantIdQuery;
import com.databit.stockia.platform.iam.application.queryservices.UserQueryService;
import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> handle(GetUsersByRestaurantIdQuery query) {
        return userRepository.findAllByRestaurantId(query.restaurantId());
    }
}
