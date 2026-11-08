package com.databit.stockia.platform.iam.interfaces.rest;

import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.shared.domain.model.valueobjects.EnumCodes;
import com.databit.stockia.platform.shared.interfaces.rest.transform.ProblemDetails;
import com.databit.stockia.platform.iam.application.commands.ChangeUserRoleCommand;
import com.databit.stockia.platform.iam.application.commands.RemoveMemberCommand;
import com.databit.stockia.platform.iam.application.commands.UnlockUserCommand;
import com.databit.stockia.platform.iam.application.commandservices.UserCommandService;
import com.databit.stockia.platform.iam.application.internal.outboundservices.acl.ExternalRestaurantService;
import com.databit.stockia.platform.iam.application.queries.GetUserByIdQuery;
import com.databit.stockia.platform.iam.application.queries.GetUsersByRestaurantIdQuery;
import com.databit.stockia.platform.iam.application.queryservices.UserQueryService;
import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;
import com.databit.stockia.platform.iam.interfaces.rest.resources.ChangePasswordResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.ChangeUserRoleResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.InviteMemberResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.UpdateUserProfileResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.UserResource;
import com.databit.stockia.platform.iam.interfaces.rest.transform.IamHttpResponseMapper;
import com.databit.stockia.platform.iam.interfaces.rest.transform.UserCommandMapper;
import com.databit.stockia.platform.iam.interfaces.rest.transform.UserResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Team of the restaurant (Roles &amp; permissions) and the profile of the signed-in user. Listing and
 * changing the team is for Administrators; every user edits only their own profile and password.
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Team members and profile")
public class UsersController {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final ExternalRestaurantService externalRestaurantService;
    private final CurrentUserProvider currentUser;
    private final RestaurantClock clock;
    private final MessageSource messageSource;
    private final String invitationPassword;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService,
                           ExternalRestaurantService externalRestaurantService, CurrentUserProvider currentUser, RestaurantClock clock,
                           MessageSource messageSource, @Value("${stockia.iam.invitation-temporary-password:stockia123}") String invitationPassword) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.externalRestaurantService = externalRestaurantService;
        this.currentUser = currentUser;
        this.clock = clock;
        this.messageSource = messageSource;
        this.invitationPassword = invitationPassword;
    }

    @Operation(summary = "List the team of the restaurant", description = "Newest first (Administrator only)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResource> getAllUsers() {
        var restaurantId = currentUser.requireRestaurantId();
        var restaurant = externalRestaurantService.getRestaurant(restaurantId);
        var now = clock.instant();
        return userQueryService.handle(new GetUsersByRestaurantIdQuery(restaurantId)).stream()
                .map(user -> UserResourceMapper.toResourceFromEntity(user, restaurant, now)).toList();
    }

    @Operation(summary = "Get the signed-in user")
    @GetMapping("me")
    public ResponseEntity<?> getCurrentUser() {
        return getUserById(currentUser.requireUserId());
    }

    @Operation(summary = "Get a team member", description = "An Employee can only read their own account")
    @GetMapping("{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        var found = userQueryService.handle(new GetUserByIdQuery(id)).filter(user -> user.getRestaurantId().equals(currentUser.requireRestaurantId()));
        if (found.isEmpty()) return problem(HttpStatus.NOT_FOUND, "UserNotFound", "iam.error.UserNotFound");
        if (!currentUser.isAdmin() && !id.equals(currentUser.requireUserId())) return forbidden();
        return ResponseEntity.ok(toResource(found.get()));
    }

    @Operation(summary = "Invite a team member", description = "Creates the member with a temporary password (a real deployment would email it)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> inviteMember(@Valid @RequestBody InviteMemberResource resource) {
        var role = EnumCodes.parse(UserRole.class, resource.role());
        if (role.isEmpty()) return invalidRole();
        var command = UserCommandMapper.toCommandFromResource(resource, currentUser.requireRestaurantId(), role.get(), invitationPassword);
        var result = userCommandService.handle(command);
        return IamHttpResponseMapper.toResponseEntity(result, messageSource,
                user -> ResponseEntity.created(URI.create("/api/v1/users/" + user.getId())).body(toResource(user)));
    }

    @Operation(summary = "Update my profile", description = "Name and email of the signed-in user")
    @PatchMapping("{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @Valid @RequestBody UpdateUserProfileResource resource) {
        if (!id.equals(currentUser.requireUserId())) return forbidden();
        var result = userCommandService.handle(UserCommandMapper.toCommandFromResource(resource, id));
        return IamHttpResponseMapper.toResponseEntity(result, messageSource, user -> ResponseEntity.ok(toResource(user)));
    }

    @Operation(summary = "Change my password", description = "The current password is verified first")
    @PostMapping("{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordResource resource) {
        if (!id.equals(currentUser.requireUserId())) return forbidden();
        var result = userCommandService.handle(UserCommandMapper.toCommandFromResource(resource, id));
        return IamHttpResponseMapper.toResponseEntity(result, messageSource, ignored -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Change the role of a member", description = "The team always keeps at least one Administrator")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @Valid @RequestBody ChangeUserRoleResource resource) {
        var role = EnumCodes.parse(UserRole.class, resource.role());
        if (role.isEmpty()) return invalidRole();
        var result = userCommandService.handle(new ChangeUserRoleCommand(currentUser.requireRestaurantId(), id, role.get()));
        return IamHttpResponseMapper.toResponseEntity(result, messageSource, user -> ResponseEntity.ok(toResource(user)));
    }

    @Operation(summary = "Unlock a team member", description = "Clears the lock after wrong passwords (Account Lock Service)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("{id}/unlock")
    public ResponseEntity<?> unlock(@PathVariable Long id) {
        var result = userCommandService.handle(new UnlockUserCommand(currentUser.requireRestaurantId(), id));
        return IamHttpResponseMapper.toResponseEntity(result, messageSource, user -> ResponseEntity.ok(toResource(user)));
    }

    @Operation(summary = "Remove a team member", description = "You cannot remove yourself nor the last Administrator")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<?> removeMember(@PathVariable Long id) {
        var result = userCommandService.handle(new RemoveMemberCommand(currentUser.requireRestaurantId(), id, currentUser.requireUserId()));
        return IamHttpResponseMapper.toResponseEntity(result, messageSource, ignored -> ResponseEntity.noContent().build());
    }

    private UserResource toResource(User user) {
        return UserResourceMapper.toResourceFromEntity(user, externalRestaurantService.getRestaurant(user.getRestaurantId()), clock.instant());
    }

    private ResponseEntity<?> forbidden() {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", "iam.error.OwnAccountOnly");
    }

    private ResponseEntity<?> invalidRole() {
        return problem(HttpStatus.BAD_REQUEST, "InvalidRole", "iam.error.InvalidRole");
    }

    private ResponseEntity<?> problem(HttpStatus status, String title, String key) {
        return ProblemDetails.response(status, title, messageSource.getMessage(key, null, LocaleContextHolder.getLocale()));
    }
}
