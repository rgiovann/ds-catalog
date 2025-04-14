//package com.devsuperior.dscatalog.components;
//
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.jwt.JwtClaimsSet;
//import org.springframework.stereotype.Component;
//
//import com.devsuperior.dscatalog.entities.User;
//import com.devsuperior.dscatalog.repositories.UserRepository;
//
//// recebe apenas o UserRepository (e, se necessário, o valor do tempo de expiração) 
//// e retorne um JwtClaimsSet customizado.
//
//@Component
//public class JwtTokenEnhancer {
//    
//    private final UserRepository userRepository;
//    
//    @Value("${jwt.duration}")
//    private Long jwtDuration;
//    
//    public JwtTokenEnhancer(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//    
//    public JwtClaimsSet enhanceToken(Authentication authentication) {
//        User user = userRepository.findByEmail(authentication.getName());
//        Instant now = Instant.now();
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("userFirstName", user.getFirstName());
//        claims.put("userId", user.getId());
//        
//        return JwtClaimsSet.builder()
//                .issuer("self")
//                .issuedAt(now)
//                .expiresAt(now.plusSeconds(jwtDuration))
//                .subject(authentication.getName())
//                .claims(c -> c.putAll(claims))
//                .build();
//    }
//}
