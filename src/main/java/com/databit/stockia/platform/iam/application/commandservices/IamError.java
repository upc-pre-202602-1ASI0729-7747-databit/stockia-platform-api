package com.databit.stockia.platform.iam.application.commandservices;

import com.databit.stockia.platform.shared.application.result.ApplicationError;

/**
 * Errors of the Identity &amp; Access Management bounded context (their codes are the problem titles).
 */
public enum IamError implements ApplicationError {
    INVALID_CREDENTIALS,
    ACCOUNT_LOCKED,
    INVALID_RESET_CODE,
    EMAIL_ALREADY_TAKEN,
    USER_NOT_FOUND,
    CURRENT_PASSWORD_INVALID,
    SAME_PASSWORD,
    LAST_ADMINISTRATOR,
    CANNOT_REMOVE_YOURSELF,
    DATABASE_ERROR;

    @Override
    public String messageKeyPrefix() {
        return "iam.error";
    }
}
