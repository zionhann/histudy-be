package edu.handong.csee.histudy.config;

import edu.handong.csee.histudy.domain.Role;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
public class SwaggerConfig {

    @Value("${custom.jwt.secret.user}")
    private String userToken;

    @Value("${custom.jwt.secret.member}")
    private String memberToken;

    @Value("${custom.jwt.secret.admin}")
    private String adminToken;

    @Bean
    public OpenAPI customizeOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(Role.USER.name(), new SecurityScheme()
                                .name(Role.USER.name())
                                .description("For user testing, use the following token:\n\n"
                                        + userToken)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes(Role.MEMBER.name(), new SecurityScheme()
                                .name(Role.MEMBER.name())
                                .description("For member testing, use the following token:\n\n"
                                        + memberToken)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes(Role.ADMIN.name(), new SecurityScheme()
                                .name(Role.ADMIN.name())
                                .description("For administrator testing, use the following token:\n\n"
                                        + adminToken)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}