package com.db.cmetal.be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.db.cmetal.be.utils.Constants;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title(Constants.OPENAPI_TITLE)
                .version(Constants.OPENAPI_VERSION)
                .description(Constants.OPENAPI_DESCRIPTION))
            .addSecurityItem(new SecurityRequirement().addList(Constants.OPENAPI_SECURITY_SCHEME))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(Constants.OPENAPI_SECURITY_SCHEME, new SecurityScheme()
                    .name(Constants.OPENAPI_SECURITY_SCHEME)
                    .type(Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .in(In.HEADER)));
    }
}
