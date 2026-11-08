package com.databit.stockia.platform.iam.application.queryservices;

import com.databit.stockia.platform.iam.application.queries.GetUserByIdQuery;
import com.databit.stockia.platform.iam.application.queries.GetUsersByRestaurantIdQuery;
import com.databit.stockia.platform.iam.domain.model.aggregates.User;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {
    Optional<User> handle(GetUserByIdQuery query);

    List<User> handle(GetUsersByRestaurantIdQuery query);
}
