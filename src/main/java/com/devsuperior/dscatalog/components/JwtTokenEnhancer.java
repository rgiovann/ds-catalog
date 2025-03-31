package com.devsuperior.dscatalog.components;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.UserRepository;

@Component
public class JwtTokenEnhancer implements TokenEnhancer {
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		User user = userRepository.findByEmail(authentication.getName());
		// add information in the token, need a Map structure
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userFirstName",user.getFirstName());
		map.put("userId",user.getId());
		// downcasting to subclass because it has the add extra info to the token
		DefaultOAuth2AccessToken enhancedToken = (DefaultOAuth2AccessToken) accessToken;
		enhancedToken.setAdditionalInformation(map);
 		return accessToken;
	}

}
