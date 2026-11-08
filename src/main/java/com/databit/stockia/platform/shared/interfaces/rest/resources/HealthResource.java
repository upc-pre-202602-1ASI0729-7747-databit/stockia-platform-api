package com.databit.stockia.platform.shared.interfaces.rest.resources;

import java.time.Instant;

public record HealthResource(String status, String service, String version, Instant time) {
}