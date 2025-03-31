package com.devsuperior.dscatalog.dto;

import com.devsuperior.dscatalog.services.validation.UserInsertOrUpdateValid;

 
@UserInsertOrUpdateValid
public class UserInsertUpdateDTO extends UserDTO {

	private static final long serialVersionUID = 1L;

	private String password;

	public UserInsertUpdateDTO() {
		super();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
