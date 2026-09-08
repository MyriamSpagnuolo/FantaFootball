package org.generation.italy.fantafootball.security.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI fantaFootballOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FantaFootball API")
                        .version("0.0.1")
                        .description("Backend API for FantaFootball authentication, leagues, teams, players, trades, matchdays, and standings.")
                        .contact(new Contact().name("FantaFootball"))
                        .license(new License().name("Private project")))
                .servers(List.of(new Server()
                        .url("http://localhost:8081")
                        .description("Local development server")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, bearerJwtScheme())
                        .addSchemas("ApiError", apiErrorSchema())
                        .addSchemas("ValidationError", validationErrorSchema())
                        .addResponses("BadRequest", errorResponse("Bad request", "#/components/schemas/ApiError"))
                        .addResponses("ValidationFailed", errorResponse("Request validation failed", "#/components/schemas/ValidationError"))
                        .addResponses("Unauthorized", errorResponse("Authentication is required", "#/components/schemas/ApiError"))
                        .addResponses("Forbidden", errorResponse("The authenticated user is not allowed to perform this action", "#/components/schemas/ApiError"))
                        .addResponses("NotFound", errorResponse("Resource not found", "#/components/schemas/ApiError"))
                        .addResponses("Conflict", errorResponse("The request conflicts with existing data", "#/components/schemas/ApiError")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public OpenApiCustomizer publicEndpointSecurityCustomizer() {
        return openApi -> openApi.getPaths().forEach(OpenApiConfig::clearSecurityForPublicOperations);
    }

    private static void clearSecurityForPublicOperations(String path, PathItem pathItem) {
        if (path.startsWith("/api/public/")) {
            pathItem.readOperations().forEach(operation -> operation.setSecurity(List.of()));
            return;
        }
        if ("/api/auth/login".equals(path) && pathItem.getPost() != null) {
            pathItem.getPost().setSecurity(List.of());
        }
        if ("/api/auth/register".equals(path) && pathItem.getPost() != null) {
            pathItem.getPost().setSecurity(List.of());
        }
        if ("/api/auth/forgot-password".equals(path) && pathItem.getPost() != null) {
            pathItem.getPost().setSecurity(List.of());
        }
        if ("/api/auth/reset-password".equals(path) && pathItem.getPost() != null) {
            pathItem.getPost().setSecurity(List.of());
        }
        if ("/api/registration-requests".equals(path) && pathItem.getPost() != null) {
            pathItem.getPost().setSecurity(List.of());
        }
    }

    private static SecurityScheme bearerJwtScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste the JWT returned by POST /api/auth/login.");
    }

    private static Schema<?> apiErrorSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("errorCode", new StringSchema().example("PLAYER_NOT_FOUND"));
        schema.addProperty("message", new StringSchema().example("Giocatore non trovato"));
        schema.required(List.of("errorCode", "message"));
        return schema;
    }

    private static Schema<?> validationErrorSchema() {
        ObjectSchema fieldErrors = new ObjectSchema();
        fieldErrors.additionalProperties(new StringSchema().example("must not be blank"));

        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("errorCode", new StringSchema().example("validation_failed"));
        schema.addProperty("message", new StringSchema().example("Request validation failed"));
        schema.addProperty("fieldErrors", fieldErrors);
        schema.required(List.of("errorCode", "message", "fieldErrors"));
        return schema;
    }

    private static ApiResponse errorResponse(String description, String schemaRef) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new Schema<>().$ref(schemaRef))
                ));
    }
}
