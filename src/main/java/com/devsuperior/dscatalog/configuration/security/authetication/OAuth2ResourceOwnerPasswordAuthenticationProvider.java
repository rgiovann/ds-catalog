package com.devsuperior.dscatalog.configuration.security.authetication;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import com.devsuperior.dscatalog.components.JwtTokenEnhancer;
import com.devsuperior.dscatalog.configuration.security.resource.OAuth2ResourceOwnerPasswordAuthenticationToken;

@Component
public class OAuth2ResourceOwnerPasswordAuthenticationProvider implements AuthenticationProvider {

	/*
    private final AuthenticationManager authenticationManager;
    private final RegisteredClientRepository registeredClientRepository;
    private final JwtEncoder jwtEncoder;
    private final JwtTokenEnhancer tokenEnhancer; // Injetado
    
    public OAuth2ResourceOwnerPasswordAuthenticationProvider(
            AuthenticationManager authenticationManager,
            RegisteredClientRepository registeredClientRepository,
            JwtEncoder jwtEncoder,
            JwtTokenEnhancer tokenEnhancer) {
        this.authenticationManager = authenticationManager;
        this.registeredClientRepository = registeredClientRepository;
        this.jwtEncoder = jwtEncoder;
        this.tokenEnhancer = tokenEnhancer;
        
        System.out.println("### JwtEncoder injetado: ### " + (jwtEncoder != null));

    }
    
    */
	
	private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RegisteredClientRepository registeredClientRepository;
    private final JwtEncoder jwtEncoder;
    private final JwtTokenEnhancer tokenEnhancer;

    public OAuth2ResourceOwnerPasswordAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            RegisteredClientRepository registeredClientRepository,
            JwtEncoder jwtEncoder,
            JwtTokenEnhancer tokenEnhancer) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.registeredClientRepository = registeredClientRepository;
        this.jwtEncoder = jwtEncoder;
        this.tokenEnhancer = tokenEnhancer;
        System.out.println("### JwtEncoder injetado: ### " + (jwtEncoder != null));
    }	
	
/*
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2ResourceOwnerPasswordAuthenticationToken authRequest = 
            (OAuth2ResourceOwnerPasswordAuthenticationToken) authentication;

        // Autentica o usuário
        UsernamePasswordAuthenticationToken userAuth = new UsernamePasswordAuthenticationToken(
            authRequest.getUsername(), authRequest.getCredentials());
        Authentication authenticatedUser = authenticationManager.authenticate(userAuth);

        // Obtém o clientPrincipal (autenticação do cliente)
        Authentication clientPrincipal = authRequest.getClientPrincipal();

        // Busca o RegisteredClient
        String clientId = clientPrincipal.getName(); // Assume que o nome do clientPrincipal é o clientId
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_client", "Client not found", null));
        }

        // Gera o access token com JWT customizado
        Instant now = Instant.now();
        Duration tokenTTL = registeredClient.getTokenSettings().getAccessTokenTimeToLive();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("http://localhost:8080")
            .issuedAt(now)
            .expiresAt(now.plus(tokenTTL))
            .subject(authenticatedUser.getName())
            .claim("firstName", authenticatedUser.getName()) // Customização
            .claim("userId", extractUserId(authenticatedUser)) // Customização
            .claim("scope", String.join(" ", registeredClient.getScopes()))
            .build();
        System.out.println("### 🔑 Tentando gerar JWT com as claims: ###" + claims);

        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            tokenValue,
            now,
            now.plus(tokenTTL)
        );
        
        System.out.println("### ✅ Token gerado com sucesso: ###" + tokenValue);

        // Gera o refresh token (opcional)
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            refreshToken = generateRefreshToken(registeredClient);
        }

        // Parâmetros adicionais (opcional)
        Map<String, Object> additionalParameters = Map.of(
            "firstName", authenticatedUser.getName(),
            "userId", extractUserId(authenticatedUser)
        );

        // Retorna o OAuth2AccessTokenAuthenticationToken
        return new OAuth2AccessTokenAuthenticationToken(
            registeredClient,
            clientPrincipal,
            accessToken,
            refreshToken,
            additionalParameters
        );
    }
    */
    
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2ResourceOwnerPasswordAuthenticationToken authRequest = 
            (OAuth2ResourceOwnerPasswordAuthenticationToken) authentication;
       
        // Autentica o usuário manualmente
        String username = authRequest.getUsername();
        String password = authRequest.getCredentials().toString();
        UserDetails user = userDetailsService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        Authentication authenticatedUser = new UsernamePasswordAuthenticationToken(
            user.getUsername(), null, user.getAuthorities());

        // Obtém o clientPrincipal (autenticação do cliente)
        Authentication clientPrincipal = authRequest.getClientPrincipal();

        // Busca o RegisteredClient
        String clientId = clientPrincipal.getName(); // Assume que o nome do clientPrincipal é o clientId
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_client", "Client not found", null));
        }

        // Usa o JwtTokenEnhancer para gerar as claims personalizadas (JWT)
        JwtClaimsSet claims = tokenEnhancer.enhanceToken(authenticatedUser);
        System.out.println("### 🔑 Tentando gerar JWT com as claims: ###" + claims.getClaims());

        // Gera o access token com JWT customizado
        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        System.out.println("### ✅ Token gerado com sucesso: ###" + tokenValue);

        // Gera o access token
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            tokenValue,
            claims.getIssuedAt(),
            claims.getExpiresAt()
        );
        

        // Gera o refresh token (opcional)
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            refreshToken = generateRefreshToken(registeredClient);
        }

        // Parâmetros adicionais (opcional)
        Map<String, Object> additionalParameters = Map.of(
            "firstName", claims.getClaim("userFirstName"),
            "userId", claims.getClaim("userId")
        );

        // Retorna o OAuth2AccessTokenAuthenticationToken
        return new OAuth2AccessTokenAuthenticationToken(
            registeredClient,
            clientPrincipal,
            accessToken,
            refreshToken,
            additionalParameters
        );
    }
    
    
    
    

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ResourceOwnerPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // Método auxiliar para gerar refresh token (exemplo)
    private OAuth2RefreshToken generateRefreshToken(RegisteredClient client) {
        Instant now = Instant.now();
        Duration refreshTTL = client.getTokenSettings().getRefreshTokenTimeToLive();
        String refreshTokenValue = UUID.randomUUID().toString(); // Simples exemplo, use algo mais seguro em produção
        return new OAuth2RefreshToken(refreshTokenValue, now, now.plus(refreshTTL));
    }

}

