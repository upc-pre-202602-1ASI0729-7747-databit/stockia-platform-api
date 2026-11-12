package com.databit.stockia.platform.iam.domain.model.valueobjects;

/**
 * Roles of the restaurant team. The Administrator has full access; the Employee cannot open Roles &amp;
 * permissions, Recommendations or the subscription.
 */
public enum UserRole {
    ADMIN,
    EMPLOYEE
}
