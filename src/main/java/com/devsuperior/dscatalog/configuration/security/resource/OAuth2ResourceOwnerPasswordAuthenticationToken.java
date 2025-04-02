package com.devsuperior.dscatalog.configuration.security.resource;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collection;
import java.util.Collections;


/**
 * Representa a autenticação via Resource Owner Password Credentials.
 * Antes da autenticação, o token contém o username e password e não possui authorities.
 * Após a autenticação, ele pode ser reconstruído com as authorities e o principal atualizado.
 */
public class OAuth2ResourceOwnerPasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken  {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String username;
    private String password;
    @SuppressWarnings("unused")
	private final Object principal;

    /**
     * Construtor para criar um token não autenticado, contendo username e password.
     *
     * @param username o nome do usuário
     * @param password a senha do usuário
     */
    public OAuth2ResourceOwnerPasswordAuthenticationToken(Authentication clientPrincipal, String username, String password) {
        super(new AuthorizationGrantType("password"), clientPrincipal, Collections.emptyMap());
        this.username = username;
        this.password = password;
        this.principal = username;
    }

    /**
     * Construtor para criar um token autenticado, contendo o principal e authorities.
     *
     * @param principal   O usuário autenticado (pode ser um objeto UserDetails)
     * @param password    A senha (geralmente não é utilizada após a autenticação)
     * @param authorities As authorities concedidas
     */
    public OAuth2ResourceOwnerPasswordAuthenticationToken(Authentication clientPrincipal, Object principal, 
            String password, Collection<? extends GrantedAuthority> authorities) {
        super(new AuthorizationGrantType("password"), clientPrincipal, Collections.emptyMap());
        
        this.principal = principal;
        this.password = password;

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            this.username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            this.username = principal.toString();
        }

        super.setAuthenticated(true); // Define como autenticado
    }

    @Override
    public Object getCredentials() {
        return this.password;
    }

    public Authentication getClientPrincipal() {
    	return (Authentication) super.getPrincipal(); // Casting seguro de Object para Authentication   
    } 
    
    /**
     * Retorna o username usado na autenticação (antes de ser autenticado).
     */
    public String getUsername() {
        return this.username;
    }
    
    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                "Não é possível definir este token como autenticado. Use o construtor que recebe authorities.");
        }
        super.setAuthenticated(false);
    }
    
    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.password = null;
    }
}

