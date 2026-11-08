package com.databit.stockia.platform.iam.infrastructure.persistence.jpa;

import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;
import com.databit.stockia.platform.iam.infrastructure.persistence.jpa.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("userJpaRepository")
public interface UserRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<UserJpaEntity> findAllByRestaurantIdOrderByIdDesc(Long restaurantId);

    long countByRestaurantIdAndRole(Long restaurantId, UserRole role);
}
