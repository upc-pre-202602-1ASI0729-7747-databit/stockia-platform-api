package com.databit.stockia.platform.iam.infrastructure.authorization.sfs.configuration;

import com.databit.stockia.platform.shared.interfaces.rest.transform.ProblemDetails;
import com.databit.stockia.platform.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.databit.stockia.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;

/**
 * Stateless security: a JWT bearer token on every request except sign-in, password recovery,
 * restaurant sign-up, the plan catalog, health and the API documentation. Roles are checked with
 * @PreAuthorize("hasRole('ADMIN')"). 401 and 403 answer with problem details.
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/authentication/**", "/api/v1/health", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**",
            "/swagger-resources/**", "/webjars/**", "/error"};

    private final BearerTokenService tokenService;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;
    private final ObjectMapper objectMapper;

    public WebSecurityConfiguration(BearerTokenService tokenService, MessageSource messageSource, LocaleResolver localeResolver,
                                    ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeProblem(response, HttpStatus.UNAUTHORIZED, "Unauthorized", "security.unauthorized", request))
                        .accessDeniedHandler((request, response, exception) ->
                                writeProblem(response, HttpStatus.FORBIDDEN, "Forbidden", "security.forbidden", request)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/restaurant-registrations").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/plans").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new BearerAuthorizationRequestFilter(tokenService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void writeProblem(HttpServletResponse response, HttpStatus status, String title, String messageKey,
                              jakarta.servlet.http.HttpServletRequest request) throws IOException {
        var locale = localeResolver.resolveLocale(request);
        var problem = ProblemDetails.of(status, title, messageSource.getMessage(messageKey, null, title, locale), null);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
