package com.databit.stockia.platform.iam.interfaces.rest;

import com.databit.stockia.platform.iam.application.commandservices.UserCommandService;
import com.databit.stockia.platform.iam.application.internal.outboundservices.acl.ExternalRestaurantService;
import com.databit.stockia.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.PasswordResetRequestResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.RequestPasswordResetResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.ResetPasswordResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.SignInResource;
import com.databit.stockia.platform.iam.interfaces.rest.transform.IamHttpResponseMapper;
import com.databit.stockia.platform.iam.interfaces.rest.transform.UserCommandMapper;
import com.databit.stockia.platform.iam.interfaces.rest.transform.UserResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Login Controller and Password Reset Service (C4). The sign-up of a new restaurant is in Restaurant
 * Registration (POST /api/v1/restaurant-registrations).
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Sign-in and password recovery")
public class AuthenticationController {
    private final UserCommandService userCommandService;
    private final ExternalRestaurantService externalRestaurantService;
    private final MessageSource messageSource;

    public AuthenticationController(UserCommandService userCommandService, ExternalRestaurantService externalRestaurantService,
                                    MessageSource messageSource) {
        this.userCommandService = userCommandService;
        this.externalRestaurantService = externalRestaurantService;
        this.messageSource = messageSource;
    }

    @Operation(summary = "Sign in", description = "Authenticates with email and password and returns the user with a JWT. After 5 "
            + "wrong passwords in a row the account is locked for 15 minutes (423 AccountLocked)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signed in", content = @Content(schema = @Schema(implementation = AuthenticatedUserResource.class))),
            @ApiResponse(responseCode = "401", description = "Wrong email or password", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "423", description = "Account locked", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @PostMapping("sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource resource) {
        var result = userCommandService.handle(UserCommandMapper.toCommandFromResource(resource));
        return IamHttpResponseMapper.toResponseEntity(result, messageSource, session -> ResponseEntity.ok(UserResourceMapper
                .toAuthenticatedResource(session.user(), externalRestaurantService.getRestaurant(session.user().getRestaurantId()), session.token())));
    }

    @Operation(summary = "Request a password recovery link", description = "\"Forgot your password?\": emails a recovery link. "
            + "Same answer whether the email is registered or not")
    @ApiResponse(responseCode = "202", description = "Request accepted", content = @Content(schema = @Schema(implementation = PasswordResetRequestResource.class)))
    @PostMapping("password-reset-requests")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody RequestPasswordResetResource resource) {
        var result = userCommandService.handle(UserCommandMapper.toCommandFromResource(resource));
        return IamHttpResponseMapper.toResponseEntity(result, messageSource, request -> ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(UserResourceMapper.toResourceFromValue(request,
                        messageSource.getMessage("iam.message.PasswordResetRequested", null, LocaleContextHolder.getLocale()))));
    }

    @Operation(summary = "Reset the password", description = "Sets a new password with the code of the recovery link (it also unlocks the account)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "422", description = "Invalid or expired link", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})
    @PostMapping("password-resets")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordResource resource) {
        var result = userCommandService.handle(UserCommandMapper.toCommandFromResource(resource));
        return IamHttpResponseMapper.toResponseEntity(result, messageSource, ignored -> ResponseEntity.noContent().build());
    }
}
