package com.databit.stockia.platform.iam.application.commands;

/**
 * Creates the owner of a new restaurant (called by Restaurant Registration through the ACL).
 */
public record CreateOwnerAccountCommand(String fullName, String email, String password, Long restaurantId) {
}
