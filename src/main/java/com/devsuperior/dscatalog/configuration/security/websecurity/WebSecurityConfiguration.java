package com.devsuperior.dscatalog.configuration.security.websecurity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.annotation.PostConstruct;

@Configuration
public class WebSecurityConfiguration {
	
    @Autowired
    private UserDetailsService userDetailsService;

    @PostConstruct
    public void verifyUser() {
        try {
            UserDetails user = userDetailsService.loadUserByUsername("maria@gmail.com");
            System.out.println("### [@PostConstruct] Usuário encontrado ###: " + user.getUsername());
        } catch (UsernameNotFoundException e) {
            System.out.println("### [@PostConstruct] Usuário não encontrado!###");
        }
    }

}

