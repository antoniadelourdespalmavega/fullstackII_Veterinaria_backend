package com.veterinaria.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Configuración de Swagger: título de la API y, si se exige token, el botón "Authorize" para pegar el JWT.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(@Value("${app.swagger.sin-token:false}") boolean sinToken) {
        OpenAPI api = new OpenAPI()
                .info(new Info().title("API Veterinaria San Marcos").version("1.0.0")
                        .description(sinToken
                                ? "Usuarios, citas y fichas clínicas. Swagger está en modo sin token: "
                                  + "las pruebas desde esta página se hacen como administrador."
                                : "Usuarios, citas y fichas clínicas. Para probar los endpoints privados: "
                                  + "haz login en POST /api/auth/login, copia el token y pégalo en el botón Authorize."));
        if (!sinToken) {
            api.addSecurityItem(new SecurityRequirement().addList("jwt"))
               .components(new Components().addSecuritySchemes("jwt",
                       new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
        }
        return api;
    }
}