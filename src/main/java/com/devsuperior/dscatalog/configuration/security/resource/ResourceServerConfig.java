package com.devsuperior.dscatalog.configuration.security.resource;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class ResourceServerConfig {

	@Value("${cors.origins}")
	private String corsOrigins;

	@Autowired
	private Environment env;

	// @Autowired
	// private JwtTokenStore tokenStore;

	private static final String[] PUBLIC = { "/oauth/token", "/h2-console/**" };
	private static final String[] OPERATOR_OR_ADMIN = { "/products/**", "/categories/**" };
	private static final String[] ADMIN = { "/users/**" };

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		// H2 - Desativa proteção para permitir acesso ao console do H2 em modo de teste
		if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
			http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
		}

		http.csrf(csrf -> csrf.disable()) // Se não estiver usando CSRF Tokens, pode desabilitar
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						
			            // 🔹 Libera os endpoints do Swagger
			            .requestMatchers("/v2/api-docs").permitAll()
			            .requestMatchers("/configuration/ui").permitAll()
			            .requestMatchers("/swagger-resources/**").permitAll()
			            .requestMatchers("/configuration/security").permitAll()
			            .requestMatchers("/swagger-ui.html").permitAll()
			            .requestMatchers("/webjars/**").permitAll()
						
						// 🔹 Define controle de acesso para as APIs
						.requestMatchers(PUBLIC).permitAll()
						.requestMatchers(HttpMethod.GET, OPERATOR_OR_ADMIN).permitAll()
						.requestMatchers(OPERATOR_OR_ADMIN).hasAnyRole("OPERATOR", "ADMIN")
						.requestMatchers(ADMIN).hasRole("ADMIN")
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())) // Define autenticação via JWT
				.cors(cors -> cors.configurationSource(corsConfigurationSource())); // Aplica configurações CORS

		return http.build();
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
