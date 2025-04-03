package com.devsuperior.dscatalog.configuration.security.authentication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.security.oauth2.core.OAuth2Error;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CustomOAuth2RefreshTokenAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        // Verifica se é uma solicitação de refresh token
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!"refresh_token".equals(grantType)) {
            return null;
        }

        // Obtém o refresh token
        String refreshToken = request.getParameter(OAuth2ParameterNames.REFRESH_TOKEN);
        if (!StringUtils.hasText(refreshToken)) {
            throw new IllegalArgumentException("Refresh token must be provided");
        }

        // Obtém o escopo (opcional)
        String scope = request.getParameter(OAuth2ParameterNames.SCOPE);
        Set<String> requestedScopes = null;
        if (StringUtils.hasText(scope)) {
            requestedScopes = new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }

        // Obtém o clientPrincipal do SecurityContext
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        if (clientPrincipal == null) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "Client authentication is required", null)
            );
        }

        // Cria um mapa para parâmetros adicionais
        Map<String, Object> additionalParameters = new HashMap<>();

        // Retorna o token de autenticação com o clientPrincipal
        return new OAuth2RefreshTokenAuthenticationToken(
            refreshToken,
            clientPrincipal, // Passa o clientPrincipal recuperado
            requestedScopes,
            additionalParameters
        );
    }
}