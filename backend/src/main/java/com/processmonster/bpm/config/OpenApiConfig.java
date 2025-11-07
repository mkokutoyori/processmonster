package com.processmonster.bpm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 *
 * Configures Swagger UI and OpenAPI documentation for the REST API.
 * Includes JWT authentication configuration and API metadata.
 *
 * Access Swagger UI at: /api/v1/swagger-ui.html
 * Access OpenAPI spec at: /api/v1/api-docs
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    /**
     * Configures OpenAPI documentation with JWT security.
     *
     * @return Configured OpenAPI instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // Security scheme for JWT
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title(appName + " API")
                .version(appVersion)
                .description("""
                    Banking Business Process Management REST API

                    ## Features
                    - Full i18n support (FR/EN) - Use Accept-Language header
                    - JWT authentication with access and refresh tokens
                    - RBAC authorization with roles and permissions
                    - BPMN 2.0 process modeling and execution
                    - Dynamic form builder and renderer
                    - Task management with notifications
                    - Audit logging for compliance
                    - Real-time dashboards and reporting

                    ## Authentication
                    1. Login via POST /api/v1/auth/login to get tokens
                    2. Use the access token in Authorization header: Bearer {token}
                    3. Refresh expired tokens via POST /api/v1/auth/refresh

                    ## Internationalization
                    Set Accept-Language header to 'en' or 'fr' for localized responses

                    ## Rate Limiting
                    API is rate-limited to 100 requests per minute per IP
                    """)
                .contact(new Contact()
                    .name("ProcessMonster Team")
                    .email("contact@processmonster.com")
                    .url("https://processmonster.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Development server"),
                new Server()
                    .url("https://processmonster-banking-bpm.herokuapp.com")
                    .description("Production server")
            ))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT authentication token. Get it from /api/v1/auth/login endpoint.")));
    }
}
