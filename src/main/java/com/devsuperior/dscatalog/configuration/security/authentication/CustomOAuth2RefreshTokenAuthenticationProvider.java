package com.devsuperior.dscatalog.configuration.security.authentication;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import com.devsuperior.dscatalog.components.JwtTokenEnhancer;

@Component
public class CustomOAuth2RefreshTokenAuthenticationProvider implements AuthenticationProvider {

    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;
    private final RegisteredClientRepository registeredClientRepository;
    private final JwtTokenEnhancer tokenEnhancer;

    public CustomOAuth2RefreshTokenAuthenticationProvider(
            JwtDecoder jwtDecoder,
            JwtEncoder jwtEncoder,
            RegisteredClientRepository registeredClientRepository,
            JwtTokenEnhancer tokenEnhancer) {
        this.jwtDecoder = jwtDecoder;
        this.jwtEncoder = jwtEncoder;
        this.registeredClientRepository = registeredClientRepository;
        this.tokenEnhancer = tokenEnhancer;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2RefreshTokenAuthenticationToken refreshAuth =
            (OAuth2RefreshTokenAuthenticationToken) authentication;

        String refreshTokenValue = refreshAuth.getRefreshToken();
        try {
            // Decodifica e valida o refresh token
            Jwt jwt = jwtDecoder.decode(refreshTokenValue);
            if (!"refresh_token".equals(jwt.getClaim("token_type"))) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
            }

            // Obtém o clientPrincipal
            Authentication clientPrincipal = (Authentication) refreshAuth.getPrincipal();
            String clientId = clientPrincipal.getName();
            RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
            if (registeredClient == null) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
            }

            // Cria uma autenticação para o usuário baseado no subject do refresh token
            Authentication userAuth = new UsernamePasswordAuthenticationToken(
                jwt.getSubject(), null, Collections.emptyList()
            );

            // Gera novas claims usando o JwtTokenEnhancer
            JwtClaimsSet newAccessClaims = tokenEnhancer.enhanceToken(userAuth);
            String newAccessTokenValue = jwtEncoder.encode(JwtEncoderParameters.from(newAccessClaims)).getTokenValue();

            OAuth2AccessToken newAccessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                newAccessTokenValue,
                newAccessClaims.getIssuedAt(),
                newAccessClaims.getExpiresAt()
            );

            // Retorna o resultado com o refresh token original
            return new OAuth2AccessTokenAuthenticationToken(
                registeredClient,
                clientPrincipal,
                newAccessToken,
                new OAuth2RefreshToken(refreshTokenValue, jwt.getIssuedAt(), jwt.getExpiresAt()),
                Map.of("firstName", jwt.getClaim("userFirstName"), "userId", jwt.getClaim("userId"))
            );
        } catch (JwtException e) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2RefreshTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}