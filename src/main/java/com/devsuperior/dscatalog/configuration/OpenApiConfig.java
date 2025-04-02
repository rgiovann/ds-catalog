package com.devsuperior.dscatalog.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("DSCatalog")
                .version("1.0")
                .description("Aplicação backend usando Springboot REST API")
                .termsOfService("Termo de uso: Open Source")
                .contact(new Contact()
                    .name("Giovanni L. Rozza")
                    .url("http://www.seusite.com.br")
                    .email("giovanni.rozza@gmail.com"))
                .license(new License()
                    .name("Licença - None")
                    .url("http://www.seusite.com.br")));
    }
}
