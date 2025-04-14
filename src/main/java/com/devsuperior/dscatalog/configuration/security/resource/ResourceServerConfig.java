package com.devsuperior.dscatalog.configuration.security.resource;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class ResourceServerConfig {

	@Value("${cors.origins}")
	private String corsOrigins;

	
	private static final String[] OPERATOR_OR_ADMIN = { "/products/**", "/categories/**" };
	private static final String[] ADMIN = { "/users/**" };
	
	
	@Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, 
    		Environment env, 
    		JwtDecoder jwtDecoder, 
    		JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
            http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        }

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints liberados explicitamente
                .requestMatchers("/keycloack/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/v2/api-docs").permitAll()
                .requestMatchers("/configuration/ui").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/configuration/security").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                // Regras de autorização
                .requestMatchers(HttpMethod.GET, OPERATOR_OR_ADMIN).permitAll()
                .requestMatchers(OPERATOR_OR_ADMIN).hasAnyRole("OPERATOR", "ADMIN")
                .requestMatchers(ADMIN).hasRole("ADMIN")
                // Todas as outras requisições exigem autenticação
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }	


	@Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles"); // Extrai roles do campo realm_access
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // Prefixo para compatibilidade com Spring Security

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		String[] origins = corsOrigins.split(",");

		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOriginPatterns(Arrays.asList(origins));
		corsConfig.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "PATCH"));
		corsConfig.setAllowCredentials(true);
		corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		return source;
	}

	@Bean
	FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(
				new CorsFilter(corsConfigurationSource()));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}

}
