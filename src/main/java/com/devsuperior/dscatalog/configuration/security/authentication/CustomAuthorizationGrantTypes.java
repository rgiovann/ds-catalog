package com.devsuperior.dscatalog.configuration.security.authentication;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

public class CustomAuthorizationGrantTypes {
    public static final AuthorizationGrantType PASSWORD = new AuthorizationGrantType("password");
    private CustomAuthorizationGrantTypes() {
        // Construtor privado para evitar instâncias acidentais
    }
}
