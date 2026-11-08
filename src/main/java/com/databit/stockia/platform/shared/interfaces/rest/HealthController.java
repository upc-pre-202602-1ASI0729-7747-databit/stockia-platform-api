package com.databit.stockia.platform.shared.interfaces.rest;

import com.databit.stockia.platform.shared.application.startup.ApplicationReadiness;
import com.databit.stockia.platform.shared.interfaces.rest.resources.HealthResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Health check used by the deployment platform and the web application (public).
 */
@RestController
@RequestMapping(value = "/api/v1/health", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Health", description = "Health check")
public class HealthController {
    private final String applicationName;
    private final String version;
    private final ApplicationReadiness applicationReadiness;

    public HealthController(@Value("${spring.application.name}") String applicationName,
                            @Value("${stockia.version:1.0.0}") String version,
                            ApplicationReadiness applicationReadiness) {
        this.applicationName = applicationName;
        this.version = version;
        this.applicationReadiness = applicationReadiness;
    }

    @Operation(summary = "Check that the API is running and its startup tasks (plan catalog, demo data) have finished")
    @ApiResponse(responseCode = "200", description = "The API is ready (status UP)")
    @ApiResponse(responseCode = "503", description = "The API is still running its startup tasks (status STARTING)")
    @GetMapping
    public ResponseEntity<HealthResource> getHealth() {
        if (!applicationReadiness.isReady())
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new HealthResource("STARTING", applicationName, version, Instant.now()));
        return ResponseEntity.ok(new HealthResource("UP", applicationName, version, Instant.now()));
    }
}