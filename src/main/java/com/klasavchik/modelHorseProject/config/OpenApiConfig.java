package com.klasavchik.modelHorseProject.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("EquiRoom API")
                        .description("REST API платформы для коллекционеров моделей лошадей. "
                                + "Управление коллекциями, проведение онлайн-шоу, судейство, регистрация участников.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("klasavch1k")
                                .url("https://github.com/klasavch1k")))
                // Глобальное требование авторизации — Swagger UI будет показывать
                // кнопку «Authorize» и подставлять токен во все запросы
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Вставьте JWT-токен, полученный через /api/v1/user/login")));
    }
}
