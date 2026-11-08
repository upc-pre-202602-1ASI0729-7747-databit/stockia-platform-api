package com.databit.stockia.platform.iam.application.internal.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.iam.application.commands.*;
import com.databit.stockia.platform.iam.application.commandservices.AuthenticatedSession;
import com.databit.stockia.platform.iam.application.commandservices.IamError;
import com.databit.stockia.platform.iam.application.commandservices.UserCommandService;
import com.databit.stockia.platform.iam.application.internal.outboundservices.acl.ExternalNotificationService;
import com.databit.stockia.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.databit.stockia.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.domain.model.valueobjects.PasswordResetRequest;
import com.databit.stockia.platform.iam.domain.model.valueobjects.SimulatedEmail;
import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;
import com.databit.stockia.platform.iam.domain.repositories.UserRepository;
import com.databit.stockia.platform.iam.domain.services.AccountLockService;
import com.databit.stockia.platform.iam.domain.services.PasswordResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Session (Login Controller, Account Lock Service), password (Password Change and Password Reset
 * Services) and team management of Identity &amp; Access Management.
 */
@Slf4j
@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final HashingService hashingService;
    private final ExternalNotificationService externalNotificationService;
    private final RestaurantClock clock;
    private final MessageSource messageSource;
    private final String resetLinkBaseUrl;
    private final boolean exposeSimulatedEmail;

    public UserCommandServiceImpl(UserRepository userRepository, TokenService tokenService, HashingService hashingService,
                                  ExternalNotificationService externalNotificationService, RestaurantClock clock, MessageSource messageSource,
                                  @Value("${stockia.iam.password-reset.reset-link-base-url:http://localhost:4200}") String resetLinkBaseUrl,
                                  @Value("${stockia.iam.password-reset.expose-simulated-email:true}") boolean exposeSimulatedEmail) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.hashingService = hashingService;
        this.externalNotificationService = externalNotificationService;
        this.clock = clock;
        this.messageSource = messageSource;
        this.resetLinkBaseUrl = resetLinkBaseUrl.replaceAll("/+$", "");
        this.exposeSimulatedEmail = exposeSimulatedEmail;
    }

    @Override
    @Transactional
    public Result<AuthenticatedSession, CommandFailure> handle(SignInCommand command) {
        var now = clock.instant();
        var found = userRepository.findByEmail(User.normalizeEmail(command.email()));
        if (found.isEmpty()) return Result.failure(CommandFailure.of(IamError.INVALID_CREDENTIALS));
        var user = found.get();
        if (user.isLockedAt(now)) return accountLocked(user, now);
        if (!hashingService.matches(command.password(), user.getPasswordHash())) {
            var locked = AccountLockService.registerFailedAttempt(user, now);
            userRepository.save(user);
            if (locked) return accountLocked(user, now);
            return Result.failure(CommandFailure.of(IamError.INVALID_CREDENTIALS).withDetails(Map.of(
                    "remainingAttempts", AccountLockService.remainingAttempts(user), "maxAttempts", AccountLockService.MAX_FAILED_ATTEMPTS)));
        }
        if (user.getFailedSignInAttempts() > 0 || user.getLockedUntil() != null) user = userRepository.save(user.unlock());
        return Result.success(new AuthenticatedSession(user, tokenService.generateToken(user)));
    }

    /**
     * The owner of a new restaurant is its first Administrator (Restaurant Registration).
     */
    @Override
    @Transactional
    public Result<AuthenticatedSession, CommandFailure> handle(CreateOwnerAccountCommand command) {
        var email = User.normalizeEmail(command.email());
        if (userRepository.existsByEmail(email)) return Result.failure(CommandFailure.of(IamError.EMAIL_ALREADY_TAKEN, email));
        var user = userRepository.save(User.create(command.fullName(), email, hashingService.encode(command.password()),
                command.restaurantId(), UserRole.ADMIN));
        return Result.success(new AuthenticatedSession(user, tokenService.generateToken(user)));
    }

    /**
     * Sends the recovery link when the email is registered. The answer is the same either way, so
     * nobody can find out which emails exist.
     */
    @Override
    @Transactional
    public Result<PasswordResetRequest, CommandFailure> handle(RequestPasswordResetCommand command) {
        var email = User.normalizeEmail(command.email());
        var masked = PasswordResetService.maskEmail(email);
        var found = userRepository.findByEmail(email);
        if (found.isEmpty()) return Result.success(new PasswordResetRequest(masked, null));
        var user = found.get();
        var code = PasswordResetService.newCode();
        var expiresAt = clock.instant().plus(PasswordResetService.CODE_LIFETIME);
        userRepository.save(user.startPasswordReset(hashingService.encode(code), expiresAt));
        var link = resetLinkBaseUrl + "/auth/reset-password?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&code=" + code;
        var locale = LocaleContextHolder.getLocale();
        var subject = messageSource.getMessage("iam.message.PasswordResetSubject", null, locale);
        var body = messageSource.getMessage("iam.message.PasswordResetBody",
                new Object[]{user.getFullName(), link, code, String.valueOf(PasswordResetService.CODE_LIFETIME.toMinutes())}, locale);
        var sent = externalNotificationService.sendEmail(user.getRestaurantId(), email, subject, body);
        if (!sent) log.warn("The recovery email for user {} could not be sent", user.getId());
        var expose = externalNotificationService.isSimulated() && exposeSimulatedEmail;
        return Result.success(new PasswordResetRequest(masked, expose ? new SimulatedEmail(email, subject, link, code, expiresAt) : null));
    }

    @Override
    @Transactional
    public Result<Void, CommandFailure> handle(ResetPasswordCommand command) {
        var now = clock.instant();
        var found = userRepository.findByEmail(User.normalizeEmail(command.email()));
        if (found.isEmpty() || !PasswordResetService.canUseCode(found.get(), now))
            return Result.failure(CommandFailure.of(IamError.INVALID_RESET_CODE));
        var user = found.get();
        if (!hashingService.matches(command.code().trim(), user.getPasswordResetCodeHash())) {
            userRepository.save(user.registerFailedPasswordReset());
            return Result.failure(CommandFailure.of(IamError.INVALID_RESET_CODE));
        }
        userRepository.save(user.updatePasswordHash(hashingService.encode(command.newPassword())).clearPasswordReset().unlock());
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result<User, CommandFailure> handle(UnlockUserCommand command) {
        var found = userRepository.findById(command.userId()).filter(user -> user.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty()) return Result.failure(CommandFailure.of(IamError.USER_NOT_FOUND));
        return Result.success(userRepository.save(found.get().unlock()));
    }

    @Override
    @Transactional
    public Result<User, CommandFailure> handle(InviteMemberCommand command) {
        var email = User.normalizeEmail(command.email());
        if (userRepository.existsByEmail(email)) return Result.failure(CommandFailure.of(IamError.EMAIL_ALREADY_TAKEN, email));
        return Result.success(userRepository.save(User.create(command.fullName(), email, hashingService.encode(command.temporaryPassword()),
                command.restaurantId(), command.role())));
    }

    /**
     * Name and email only; the password has its own command.
     */
    @Override
    @Transactional
    public Result<User, CommandFailure> handle(UpdateUserProfileCommand command) {
        var found = userRepository.findById(command.userId());
        if (found.isEmpty()) return Result.failure(CommandFailure.of(IamError.USER_NOT_FOUND));
        var email = User.normalizeEmail(command.email());
        if (userRepository.existsByEmailAndIdNot(email, command.userId()))
            return Result.failure(CommandFailure.of(IamError.EMAIL_ALREADY_TAKEN, email));
        return Result.success(userRepository.save(found.get().updateProfile(command.fullName(), email)));
    }

    /**
     * Password Change Service: the current password is verified before saving the new one.
     */
    @Override
    @Transactional
    public Result<Void, CommandFailure> handle(ChangePasswordCommand command) {
        var found = userRepository.findById(command.userId());
        if (found.isEmpty()) return Result.failure(CommandFailure.of(IamError.USER_NOT_FOUND));
        var user = found.get();
        if (!hashingService.matches(command.currentPassword(), user.getPasswordHash()))
            return Result.failure(CommandFailure.of(IamError.CURRENT_PASSWORD_INVALID));
        if (command.currentPassword().equals(command.newPassword())) return Result.failure(CommandFailure.of(IamError.SAME_PASSWORD));
        userRepository.save(user.updatePasswordHash(hashingService.encode(command.newPassword())));
        return Result.success(null);
    }

    /**
     * The team always keeps at least one Administrator.
     */
    @Override
    @Transactional
    public Result<User, CommandFailure> handle(ChangeUserRoleCommand command) {
        var found = userRepository.findById(command.userId()).filter(user -> user.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty()) return Result.failure(CommandFailure.of(IamError.USER_NOT_FOUND));
        var user = found.get();
        if (user.getRole() == command.role()) return Result.success(user);
        if (user.isAdmin() && userRepository.countAdmins(command.restaurantId()) <= 1)
            return Result.failure(CommandFailure.of(IamError.LAST_ADMINISTRATOR));
        return Result.success(userRepository.save(user.changeRole(command.role())));
    }

    @Override
    @Transactional
    public Result<Void, CommandFailure> handle(RemoveMemberCommand command) {
        var found = userRepository.findById(command.userId()).filter(user -> user.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty()) return Result.failure(CommandFailure.of(IamError.USER_NOT_FOUND));
        var user = found.get();
        if (user.getId().equals(command.actingUserId())) return Result.failure(CommandFailure.of(IamError.CANNOT_REMOVE_YOURSELF));
        if (user.isAdmin() && userRepository.countAdmins(command.restaurantId()) <= 1)
            return Result.failure(CommandFailure.of(IamError.LAST_ADMINISTRATOR));
        userRepository.delete(user);
        return Result.success(null);
    }

    private static Result<AuthenticatedSession, CommandFailure> accountLocked(User user, Instant now) {
        var minutesLeft = AccountLockService.minutesLeft(user, now);
        var details = new LinkedHashMap<String, Object>();
        details.put("lockedUntil", user.getLockedUntil());
        details.put("minutesLeft", minutesLeft);
        return Result.failure(CommandFailure.of(IamError.ACCOUNT_LOCKED, minutesLeft).withDetails(details));
    }
}
