package com.uni.ethesis.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "eThesis API",
                version = "v1",
                description = "API documentation for the eThesis application",
                contact = @Contact(
                        name = "Your Name/Team",
                        email = "your.email@example.com",
                        url = "https://yourwebsite.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development Server",
                        url = "http://localhost:8084"
                )
        },
        security = {
                @SecurityRequirement(name = "keycloak_oauth")
        }
)
@SecurityScheme(
        name = "keycloak_oauth",
        type = SecuritySchemeType.OAUTH2,
        in = SecuritySchemeIn.HEADER,
        bearerFormat = "JWT",
        flows = @OAuthFlows(
                implicit = @OAuthFlow(
                        authorizationUrl = "${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/auth",
                        scopes = {
                                @OAuthScope(name = "openid", description = "OpenID Connect scope"),
                                @OAuthScope(name = "profile", description = "Profile scope"),
                                @OAuthScope(name = "roles", description = "Roles scope")
                        }
                )
        )
)
public class OpenApiConfig {
}