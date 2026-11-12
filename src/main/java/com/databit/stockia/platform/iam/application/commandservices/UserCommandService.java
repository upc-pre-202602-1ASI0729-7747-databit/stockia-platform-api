package com.databit.stockia.platform.iam.application.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.iam.application.commands.*;
import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.domain.model.valueobjects.PasswordResetRequest;

/**
 * Session (Login Controller, Account Lock Service), password (Password Change and Password Reset
 * Services) and team management of Identity &amp; Access Management.
 */
public interface UserCommandService {
    Result<AuthenticatedSession, CommandFailure> handle(SignInCommand command);

    Result<AuthenticatedSession, CommandFailure> handle(CreateOwnerAccountCommand command);

    Result<PasswordResetRequest, CommandFailure> handle(RequestPasswordResetCommand command);

    Result<Void, CommandFailure> handle(ResetPasswordCommand command);

    Result<User, CommandFailure> handle(UnlockUserCommand command);

    Result<User, CommandFailure> handle(InviteMemberCommand command);

    Result<User, CommandFailure> handle(UpdateUserProfileCommand command);

    Result<Void, CommandFailure> handle(ChangePasswordCommand command);

    Result<User, CommandFailure> handle(ChangeUserRoleCommand command);

    Result<Void, CommandFailure> handle(RemoveMemberCommand command);
}
