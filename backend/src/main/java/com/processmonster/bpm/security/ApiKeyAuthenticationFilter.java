package com.processmonster.bpm.security;

import com.processmonster.bpm.entity.ApiKey;
import com.processmonster.bpm.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter to authenticate requests using API key (X-API-Key header).
 * This provides an alternative authentication method to JWT tokens.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String apiKeyHeader = request.getHeader(API_KEY_HEADER);

        // Only process if API key header is present and no authentication exists
        if (apiKeyHeader != null && !apiKeyHeader.isEmpty() &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                // Authenticate API key
                ApiKey apiKey = apiKeyService.authenticateApiKey(apiKeyHeader);

                if (apiKey != null && apiKey.isActive()) {
                    // Convert API key permissions to Spring Security authorities
                    List<SimpleGrantedAuthority> authorities = apiKey.getPermissions().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // Create authentication token
                    // Use the API key name as the principal
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    "apikey:" + apiKey.getName(),
                                    null,
                                    authorities
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Update last used timestamp (async to avoid blocking)
                    apiKeyService.updateLastUsed(apiKey.getId());

                    log.debug("API key authenticated: {}", apiKey.getName());
                }
            } catch (Exception e) {
                log.warn("API key authentication failed: {}", e.getMessage());
                // Don't throw exception, just continue without authentication
                // The security configuration will handle unauthorized access
            }
        }

        filterChain.doFilter(request, response);
    }
}
