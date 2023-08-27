package edu.handong.csee.histudy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${custom.jwt.secret.general}")
    private String generalToken;

    @Value("${custom.jwt.secret.admin}")
    private String adminToken;

    @Bean
    public OpenAPI customizeOpenAPI() {
        String general = "General";
        String admin = "Admin";

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(general, new SecurityScheme()
                                .name(general)
                                .description("For general user testing, use the following token:\n\n"
                                        + generalToken)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes(admin, new SecurityScheme()
                                .name(admin)
                                .description("For administrator testing, use the following token:\n\n"
                                        + adminToken)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}