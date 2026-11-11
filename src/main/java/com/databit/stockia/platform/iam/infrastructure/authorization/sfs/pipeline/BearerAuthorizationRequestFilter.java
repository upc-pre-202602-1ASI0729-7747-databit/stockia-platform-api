package com.databit.stockia.platform.iam.infrastructure.authorization.sfs.pipeline;

import com.databit.stockia.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates the bearer token of each request and stores the AuthenticatedUser (with ROLE_ADMIN or
 * ROLE_EMPLOYEE) in the security context. Requests without a valid token stay anonymous.
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {
    private final BearerTokenService tokenService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var token = tokenService.getBearerTokenFrom(request);
        if (token != null) tokenService.validateToken(token).ifPresent(user -> {
            var authentication = new UsernamePasswordAuthenticationToken(user, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        });
        filterChain.doFilter(request, response);
    }
}
