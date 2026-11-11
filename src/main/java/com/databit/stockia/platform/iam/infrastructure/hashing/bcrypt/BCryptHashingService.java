package com.databit.stockia.platform.iam.infrastructure.hashing.bcrypt;

import com.databit.stockia.platform.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt hashing for passwords and password-reset codes.
 */
public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
