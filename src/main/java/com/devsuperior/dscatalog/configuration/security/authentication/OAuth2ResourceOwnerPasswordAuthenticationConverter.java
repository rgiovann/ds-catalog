package com.devsuperior.dscatalog.configuration.security.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

import com.devsuperior.dscatalog.configuration.security.resource.OAuth2ResourceOwnerPasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

@Component
public class OAuth2ResourceOwnerPasswordAuthenticationConverter  
        implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!"password".equals(grantType)) {
            return null;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Authentication clientPrincipal = (Authentication) request.getUserPrincipal(); // Obtém o client

        if (clientPrincipal == null) {
            throw new IllegalArgumentException("Client authentication is required");
        }

        //System.out.println("### Criando OAuth2ResourceOwnerPasswordAuthenticationToken para ###: " + username);
        //System.out.println("### String password ###: " + CustomAuthorizationGrantTypes.PASSWORD.getValue());

        return new OAuth2ResourceOwnerPasswordAuthenticationToken(clientPrincipal, username, password);
    }
}

