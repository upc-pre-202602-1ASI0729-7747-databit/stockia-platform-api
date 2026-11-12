package com.databit.stockia.platform.restaurantregistration.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.entities.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Table restaurants.
 */
@Getter
@Setter
@Entity
@Table(name = "Restaurant")
public class RestaurantJpaEntity extends AuditableJpaEntity {
    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String address = "";

    @Column(nullable = false, length = 30)
    private String phone = "";

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "owner_user_id")
    private Long ownerUserId;
}
