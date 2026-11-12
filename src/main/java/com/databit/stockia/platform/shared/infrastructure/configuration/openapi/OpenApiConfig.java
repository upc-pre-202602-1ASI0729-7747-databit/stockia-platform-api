package com.databit.stockia.platform.shared.infrastructure.configuration.openapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Central OpenAPI metadata shared by all bounded contexts, with the JWT bearer scheme so the
 * protected endpoints can be tried from Swagger UI (/swagger-ui/index.html).
 */
@Configuration
public class OpenApiConfig {
    private static final String BEARER = "bearerAuthentication";

    @Bean
    public OpenAPI stockiaPlatformOpenApi(@Value("${spring.application.name}") String applicationName) {
        return new OpenAPI()
                .info(new Info()
                        .title("StockIA Platform API")
                        .version("1.0.0")
                        .description("RESTful API of StockIA (DataBite Corp): smart inventory for restaurants with demand " +
                                "forecasting (machine learning). Application: " + applicationName + ".")
                        .contact(new Contact().name("DataBit Team").url("https://github.com/upc-pre-202602-1ASI0729-7747-databit/stockia-platform-api"))                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT returned by POST /api/v1/authentication/sign-in")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}