package com.devsuperior.dscatalog.configuration.security.authentication;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;

import com.devsuperior.dscatalog.configuration.security.resource.OAuth2ResourceOwnerPasswordAuthenticationToken;

@Configuration
public class AuthorizationServerConfig {

	@Value("${security.oauth2.client.client-id}")
	private String clientId;

	@Value("${security.oauth2.client.client-secret}")
	private String clientSecret;

	@Value("${jwt.duration}")
	private Integer jwtDuration;
  
	public AuthorizationServerConfig() { 
	}
	
	@Bean
	SecurityFilterChain authorizationServerSecurityFilterChain(
	        HttpSecurity http,
	        OAuth2ResourceOwnerPasswordAuthenticationProvider customAuthProvider,
	        CustomOAuth2RefreshTokenAuthenticationProvider refreshTokenProvider,
	        OAuth2ResourceOwnerPasswordAuthenticationConverter passwordConverter,
	        CustomOAuth2RefreshTokenAuthenticationConverter refreshTokenConverter) throws Exception {
        http.securityMatcher("/oauth2/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/oauth2/token").permitAll()
                .anyRequest().authenticated())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/oauth2/token"))
            .with(OAuth2AuthorizationServerConfigurer.authorizationServer(), customizer -> {
                customizer.tokenEndpoint(tokenEndpoint -> {
                    // Configura os conversores usando accessTokenRequestConverters
                	// DelegatingAuthenticationConverter(List<AuthenticationConverter>) 
                	// is deprecated since version 1.4
                    tokenEndpoint.accessTokenRequestConverters(converters ->
                        converters.addAll(List.of( passwordConverter,refreshTokenConverter))
                    );
                });
            })
            .authenticationProvider(customAuthProvider)
            .authenticationProvider(refreshTokenProvider);

        return http.build();
    }


//	@Bean
//    OAuth2ResourceOwnerPasswordAuthenticationConverter oAuth2ResourceOwnerPasswordAuthenticationConverter() {
//        return new OAuth2ResourceOwnerPasswordAuthenticationConverter();
//    }
//	
//	@Bean
//	CustomOAuth2RefreshTokenAuthenticationConverter customOAuth2RefreshTokenAuthenticationConverter() {
//        return new CustomOAuth2RefreshTokenAuthenticationConverter();
//    }
	
	@Bean
	AuthenticationConverter authenticationConverter() {
	    return request -> {
	        String grantType = request.getParameter("grant_type");
	        if (!CustomAuthorizationGrantTypes.PASSWORD.getValue().equals(grantType)) {
	        	System.err.println("grantType : " + grantType);
	        	System.err.println("CustomAuthorizationGrantTypes.PASSWORD.getValue().: " + CustomAuthorizationGrantTypes.PASSWORD.getValue());
	            return null;
	        }

	        String username = request.getParameter("username");
	        String password = request.getParameter("password");

	        if (username == null || password == null) {
	            throw new IllegalArgumentException("Username and password must be provided");
	        }

	        // O client está autenticado antes de requisitar o token, então obtemos do SecurityContext
	        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
	        
	        if (clientPrincipal == null) {
	            throw new IllegalArgumentException("Client authentication is required");
	        }
	        
	        return new OAuth2ResourceOwnerPasswordAuthenticationToken(clientPrincipal, username, password);
	    };
	}


/*

1.AuthorizationServerConfig define o SecurityFilterChain, que depende do 
OAuth2ResourceOwnerPasswordAuthenticationProvider (via customAuthProvider).
2.OAuth2ResourceOwnerPasswordAuthenticationProvider depende do RegisteredClientRepository.

RegisteredClientRepository é um @Bean definido dentro de AuthorizationServerConfig.
Quando o Spring tenta inicializar o SecurityFilterChain, ele precisa do customAuthProvider, 
que por sua vez precisa do registeredClientRepository. No entanto, o registeredClientRepository 
só é criado após o AuthorizationServerConfig estar totalmente inicializado, formando o ciclo:	

 */
	
// TRANSFORMADO EM CLASSE POR CAUSA DO CICLO
	
//	@Bean
//	RegisteredClientRepository registeredClientRepository() {
//		RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString()).clientId(clientId)
//				.clientSecret(passwordEncoder.encode(clientSecret)).scope("read").scope("write")
//				//.clientSecret("{noop}" + clientSecret).scope("read").scope("write")
//				.authorizationGrantType(CustomAuthorizationGrantTypes.PASSWORD) // não segue boas praticas de seguranca
//				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
//				.tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofSeconds(jwtDuration))
//						.refreshTokenTimeToLive(Duration.ofSeconds(jwtDuration)).build())
//				.build();
//		return new InMemoryRegisteredClientRepository(registeredClient);
//	}
//	
//	@PostConstruct
//	public void printRegisteredClients() {
//	    RegisteredClient client = registeredClientRepository().findByClientId(clientId);
//	    if (client != null) {
//	        System.out.println("Client encontrado: " + client.getClientId());
//	    } else {
//	        System.out.println("Nenhum client registrado!");
//	    }
//	}

	@Bean
	AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().tokenEndpoint("/oauth2/token").build();
	}

}
