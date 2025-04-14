package com.devsuperior.dscatalog.configuration.security;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/keycloack/auth")
public class AuthController {
	
	@Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.redirect-uri}")
    private String redirectUri;

    @Value("${keycloak.token-endpoint}")
    private String tokenEndpoint;

    @Value("${keycloak.issuer-uri}")
    private String issuerUri;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error) throws Exception {

        if (error != null) {
            throw new IllegalStateException("Erro na autenticação com o Keycloak: " + error);
        }

        // Validação do state (proteção contra CSRF)
        // Em um ambiente real, você deve comparar o state com o valor enviado na requisição inicial
        if (state == null || state.isEmpty()) {
            throw new IllegalStateException("Parâmetro 'state' é obrigatório.");
        }

        // Configurações fixas para o fluxo PKCE
        //String clientId = "dscatalog-client";
        //String redirectUri = "http://localhost:8080/keycloack/auth/callback";
        String codeVerifier = "MyCustomCodeVerifierWithAtLeast43Characters12345"; // Deve ser dinâmico em produção

        // Monta a requisição para o Keycloak
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("code_verifier", codeVerifier);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // Usa ParameterizedTypeReference para especificar o tipo genérico Map<String, Object>
        // motivo: o método restTemplate.postForEntity() retorna ResponseEntity<Map>
        // (um tipo genérico bruto - raw type)
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};

        // Troca o code pelo token com o Keycloak
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        		tokenEndpoint,
                HttpMethod.POST,
                request,
                responseType
        );

        Map<String, Object> tokenResponse = response.getBody();
        String accessToken = (String) tokenResponse.get("access_token");

        // Decodifica o token para extrair claims customizados
        JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        Jwt jwt = jwtDecoder.decode(accessToken);

        // Adiciona os claims customizados
        tokenResponse.put("firstName", jwt.getClaimAsString("userFirstName"));
        tokenResponse.put("userId", jwt.getClaimAsString("userId"));
        tokenResponse.put("expires_in", jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
        return ResponseEntity.ok(tokenResponse);
    }

     // *** PROD - codigo não pode ser estático. ***
//    private String generateCodeVerifier() {
//        SecureRandom secureRandom = new SecureRandom();
//        byte[] codeVerifierBytes = new byte[32]; // 43-128 caracteres após codificação
//        secureRandom.nextBytes(codeVerifierBytes);
//        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
//    }
}