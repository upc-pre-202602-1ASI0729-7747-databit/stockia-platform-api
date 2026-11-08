package com.databit.stockia.platform.iam.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;
import com.databit.stockia.platform.iam.domain.repositories.UserRepository;
import com.databit.stockia.platform.iam.infrastructure.persistence.jpa.mappers.UserPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {
    private final com.databit.stockia.platform.iam.infrastructure.persistence.jpa.UserRepository jpa;
    private final UserPersistenceMapper mapper;

    public UserRepositoryAdapter(com.databit.stockia.platform.iam.infrastructure.persistence.jpa.UserRepository jpa, UserPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        var entity = mapper.toEntity(user);
        if (user.getId() != null) jpa.findById(user.getId()).ifPresent(existing -> entity.setCreatedAt(existing.getCreatedAt()));
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long exceptUserId) {
        return jpa.existsByEmailAndIdNot(email, exceptUserId);
    }

    @Override
    public List<User> findAllByRestaurantId(Long restaurantId) {
        return jpa.findAllByRestaurantIdOrderByIdDesc(restaurantId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countAdmins(Long restaurantId) {
        return jpa.countByRestaurantIdAndRole(restaurantId, UserRole.ADMIN);
    }

    @Override
    public void delete(User user) {
        jpa.deleteById(user.getId());
    }
}
