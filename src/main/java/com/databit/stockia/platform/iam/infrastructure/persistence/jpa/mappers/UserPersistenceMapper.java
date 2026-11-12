package com.databit.stockia.platform.iam.infrastructure.persistence.jpa.mappers;

import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.infrastructure.persistence.jpa.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {
    public UserJpaEntity toEntity(User user) {
        var entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setFullName(user.getFullName());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setRestaurantId(user.getRestaurantId());
        entity.setRole(user.getRole());
        entity.setFailedSignInAttempts(user.getFailedSignInAttempts());
        entity.setLockedUntil(user.getLockedUntil());
        entity.setPasswordResetCodeHash(user.getPasswordResetCodeHash());
        entity.setPasswordResetExpiresAt(user.getPasswordResetExpiresAt());
        entity.setPasswordResetAttempts(user.getPasswordResetAttempts());
        return entity;
    }

    public User toDomain(UserJpaEntity entity) {
        return User.rehydrate(entity.getId(), entity.getFullName(), entity.getEmail(), entity.getPasswordHash(), entity.getRestaurantId(),
                entity.getRole(), entity.getFailedSignInAttempts(), entity.getLockedUntil(), entity.getPasswordResetCodeHash(),
                entity.getPasswordResetExpiresAt(), entity.getPasswordResetAttempts());
    }
}
