package com.devsuperior.dscatalog.configuration.security.jwt;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.annotation.PostConstruct;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;
    
    
    @Bean
    JWKSource<SecurityContext> jwkSource() throws Exception {
        // Gera um par de chaves RSA (pública e privada)
        RSAKey rsaKey = new RSAKeyGenerator(2048) // Tamanho da chave: 2048 bits
            .keyID("my-rsa-key-id")
            .generate();
        JWKSet jwkSet = new JWKSet(rsaKey);
        //System.out.println("### Chave JWK criada: ### " + rsaKey.toJSONString());
        //  chave pública
        //System.out.println("### Chave Pública RSA: ### " + rsaKey.toPublicJWK().toJSONString());
        return (jwkSelector, securityContext) -> {
            List<JWK> jwks = jwkSelector.select(jwkSet);
            //System.out.println("### JWKs retornados pelo JwkSource: " + jwks.size());
            //jwks.forEach(j -> System.out.println("### JWK: " + j.toJSONString()));
            return jwks;
        };
    }

    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) throws Exception {
        // Extrai a chave pública do JwkSource para validação
        RSAKey rsaKey = (RSAKey) jwkSource.get(new JWKSelector(new JWKMatcher.Builder().build()), null).get(0);
        RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
 
    
//    @PostConstruct
//    public void logSecret() {
//        System.out.println("### JWT Secret carregado: " + jwtSecret);
//        
//        if (jwtSecret == null || jwtSecret.isBlank()) {
//            System.out.println("⚠️ ERRO: jwtSecret não foi carregado corretamente!");
//        } else {
//            System.out.println("✅ jwtSecret carregado corretamente com " + jwtSecret.getBytes().length + " bytes.");
//        }
//    }
}
