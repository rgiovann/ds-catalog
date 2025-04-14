package com.devsuperior.dscatalog.configuration.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

	@Value("${keycloak.certs-endpoint}")
    private String certsEndpoint;
	
    @Bean
    JwtDecoder jwtDecoder() {
        // Configura o JwtDecoder para buscar a chave pública do Keycloak
        return NimbusJwtDecoder
            .withJwkSetUri(certsEndpoint)
            .build();
    }
 
}
