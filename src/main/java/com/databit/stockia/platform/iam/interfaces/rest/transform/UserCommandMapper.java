package com.databit.stockia.platform.iam.interfaces.rest.transform;

import com.databit.stockia.platform.iam.application.commands.*;
import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;
import com.databit.stockia.platform.iam.interfaces.rest.resources.*;

public final class UserCommandMapper {
    private UserCommandMapper() {
    }

    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }

    public static RequestPasswordResetCommand toCommandFromResource(RequestPasswordResetResource resource) {
        return new RequestPasswordResetCommand(resource.email());
    }

    public static ResetPasswordCommand toCommandFromResource(ResetPasswordResource resource) {
        return new ResetPasswordCommand(resource.email(), resource.code(), resource.newPassword());
    }

    public static InviteMemberCommand toCommandFromResource(InviteMemberResource resource, Long restaurantId, UserRole role, String defaultPassword) {
        var password = resource.temporaryPassword() == null || resource.temporaryPassword().isBlank() ? defaultPassword : resource.temporaryPassword();
        return new InviteMemberCommand(restaurantId, resource.fullName(), resource.email(), role, password);
    }

    public static UpdateUserProfileCommand toCommandFromResource(UpdateUserProfileResource resource, Long userId) {
        return new UpdateUserProfileCommand(userId, resource.fullName(), resource.email());
    }

    public static ChangePasswordCommand toCommandFromResource(ChangePasswordResource resource, Long userId) {
        return new ChangePasswordCommand(userId, resource.currentPassword(), resource.newPassword());
    }
}
