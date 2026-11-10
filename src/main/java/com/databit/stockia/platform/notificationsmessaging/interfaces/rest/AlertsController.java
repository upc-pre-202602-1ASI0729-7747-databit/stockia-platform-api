package com.databit.stockia.platform.notificationsmessaging.interfaces.rest;

import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import com.databit.stockia.platform.shared.domain.model.valueobjects.EnumCodes;
import com.databit.stockia.platform.shared.interfaces.rest.transform.ProblemDetails;
import com.databit.stockia.platform.notificationsmessaging.application.commands.*;
import com.databit.stockia.platform.notificationsmessaging.application.commandservices.AlertCommandService;
import com.databit.stockia.platform.notificationsmessaging.application.internal.outboundservices.AlertMessageResolver;
import com.databit.stockia.platform.notificationsmessaging.application.queries.GetAlertsByRestaurantIdQuery;
import com.databit.stockia.platform.notificationsmessaging.application.queryservices.AlertQueryService;
import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertChannel;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertSeverity;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertType;
import com.databit.stockia.platform.notificationsmessaging.interfaces.rest.resources.AlertResource;
import com.databit.stockia.platform.notificationsmessaging.interfaces.rest.resources.SaveAlertResource;
import com.databit.stockia.platform.notificationsmessaging.interfaces.rest.transform.AlertResourceMapper;
import com.databit.stockia.platform.notificationsmessaging.interfaces.rest.transform.NotificationsMessagingHttpResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Stock, expiration and IoT alerts (C4: Alert Service).
 */
@RestController
@RequestMapping(value = "/api/v1/alerts", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Alerts", description = "Stock, expiration and IoT alerts")
public class AlertsController {
    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;
    private final AlertMessageResolver messageResolver;
    private final CurrentUserProvider currentUser;
    private final MessageSource messageSource;

    public AlertsController(AlertCommandService alertCommandService, AlertQueryService alertQueryService, AlertMessageResolver messageResolver,
                            CurrentUserProvider currentUser, MessageSource messageSource) {
        this.alertCommandService = alertCommandService;
        this.alertQueryService = alertQueryService;
        this.messageResolver = messageResolver;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Operation(summary = "List the alerts", description = "Newest first")
    @GetMapping
    public List<AlertResource> getAllAlerts() {
        return alertQueryService.handle(new GetAlertsByRestaurantIdQuery(currentUser.requireRestaurantId())).stream().map(this::toResource).toList();
    }

    @Operation(summary = "Create an alert manually")
    @PostMapping
    public ResponseEntity<?> createAlert(@Valid @RequestBody SaveAlertResource resource) {
        var codes = parse(resource);
        if (codes == null) return invalidCodes();
        var result = alertCommandService.handle(new CreateAlertCommand(currentUser.requireRestaurantId(), codes.type(), codes.severity(),
                resource.message(), codes.channel()));
        return NotificationsMessagingHttpResponseMapper.toResponseEntity(result, messageSource,
                alert -> ResponseEntity.created(URI.create("/api/v1/alerts/" + alert.getId())).body(toResource(alert)));
    }

    @Operation(summary = "Edit an alert")
    @PutMapping("{id}")
    public ResponseEntity<?> updateAlert(@PathVariable Long id, @Valid @RequestBody SaveAlertResource resource) {
        var codes = parse(resource);
        if (codes == null) return invalidCodes();
        var result = alertCommandService.handle(new UpdateAlertCommand(id, currentUser.requireRestaurantId(), codes.type(), codes.severity(),
                resource.message(), codes.channel()));
        return NotificationsMessagingHttpResponseMapper.toResponseEntity(result, messageSource, alert -> ResponseEntity.ok(toResource(alert)));
    }

    @Operation(summary = "Delete an alert")
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteAlert(@PathVariable Long id) {
        var result = alertCommandService.handle(new DeleteAlertCommand(id, currentUser.requireRestaurantId()));
        return NotificationsMessagingHttpResponseMapper.toResponseEntity(result, messageSource, ignored -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Mark an alert as attended", description = "409 NotDelivered while a required channel is pending")
    @PostMapping("{id}/acknowledge")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable Long id) {
        var result = alertCommandService.handle(new AcknowledgeAlertCommand(id, currentUser.requireRestaurantId()));
        return NotificationsMessagingHttpResponseMapper.toResponseEntity(result, messageSource, alert -> ResponseEntity.ok(toResource(alert)));
    }

    @Operation(summary = "Retry the delivery of an alert", description = "Critical alerts need WhatsApp and email")
    @PostMapping("{id}/deliveries")
    public ResponseEntity<?> retryDelivery(@PathVariable Long id) {
        var result = alertCommandService.handle(new RetryAlertDeliveryCommand(id, currentUser.requireRestaurantId()));
        return NotificationsMessagingHttpResponseMapper.toResponseEntity(result, messageSource, alert -> ResponseEntity.ok(toResource(alert)));
    }

    private AlertResource toResource(Alert alert) {
        return AlertResourceMapper.toResourceFromEntity(alert, messageResolver, LocaleContextHolder.getLocale());
    }

    private record AlertCodes(AlertType type, AlertSeverity severity, AlertChannel channel) {
    }

    private static AlertCodes parse(SaveAlertResource resource) {
        var type = EnumCodes.parse(AlertType.class, resource.type());
        var severity = EnumCodes.parse(AlertSeverity.class, resource.severity());
        var channel = EnumCodes.parse(AlertChannel.class, resource.channel());
        if (type.isEmpty() || severity.isEmpty() || channel.isEmpty()) return null;
        return new AlertCodes(type.get(), severity.get(), channel.get());
    }

    private ResponseEntity<?> invalidCodes() {
        return ProblemDetails.response(HttpStatus.BAD_REQUEST, "InvalidAlert",
                messageSource.getMessage("alert.error.InvalidAlert", null, LocaleContextHolder.getLocale()));
    }
}