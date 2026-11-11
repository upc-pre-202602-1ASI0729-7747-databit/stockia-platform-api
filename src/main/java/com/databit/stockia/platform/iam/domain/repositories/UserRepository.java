package com.databit.stockia.platform.iam.domain.repositories;

import com.databit.stockia.platform.iam.domain.model.aggregates.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long exceptUserId);

    /**
     * @return the team of a restaurant, newest first
     */
    List<User> findAllByRestaurantId(Long restaurantId);

    long countAdmins(Long restaurantId);

    void delete(User user);
}
