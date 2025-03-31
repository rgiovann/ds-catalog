package com.devsuperior.dscatalog.services.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

import com.devsuperior.dscatalog.dto.UserInsertUpdateDTO;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.resources.exceptions.FieldMessage;

public class UserInsertOrUpdateValidator implements ConstraintValidator<UserInsertOrUpdateValid, UserInsertUpdateDTO> {
	
	@Autowired 
	private UserRepository repository;
	
	@Autowired 
	private HttpServletRequest request;
	
	@Override
	public void initialize(UserInsertOrUpdateValid ann) {
		 
	}

	
	
	@Override
	public boolean isValid(UserInsertUpdateDTO dto, ConstraintValidatorContext context) {
		
		@SuppressWarnings("unchecked")
		var uriVars = (Map<String,String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		List<FieldMessage> list = new ArrayList<>();
		User user = repository.findByEmail(dto.getEmail());
 		String methodType = request.getMethod();	
		
 		// add here validation tests for User entity (add/update)
 		
 		if (Objects.equals(methodType, "PUT")) {
 			long userId = Long.parseLong(uriVars.get("id"));
 			if (user != null && userId != user.getId() ) {
 				list.add(new FieldMessage("email","Email already exists."));
 			}			
 		} else if (Objects.equals(methodType, "POST")){
  			if (user != null) {
 				list.add(new FieldMessage("email","Email already exists."));
 			}
 		}

		for (FieldMessage e : list) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		return list.isEmpty();
	}
}
