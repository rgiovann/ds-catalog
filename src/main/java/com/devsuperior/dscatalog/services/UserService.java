package com.devsuperior.dscatalog.services;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.RoleDTO;
import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.dto.UserInsertUpdateDTO;
import com.devsuperior.dscatalog.entities.Role;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.RoleRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.NestedResourceNotFoundException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class UserService implements UserDetailsService {
	
	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private BCryptPasswordEncoder pwdEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private ModelMapper modelMapper;

	// ACID properties
	@Transactional(readOnly = true)
	public Page<UserDTO> findAllPaged(Pageable pageRequest) {
		Page<User> list = userRepository.findAll(pageRequest);
		return list.map(p -> EntityToDTO(p));
	}

	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		Optional<User> obj = userRepository.findById(id);
		User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Error. Id not found: " + id));
		return EntityToDTO(entity);
	}

	@Transactional
	public UserDTO insert(UserInsertUpdateDTO userInsertDTO) {
		User entity = new User();
		entity = DTOToEntity(userInsertDTO);
		entity.setPassword(pwdEncoder.encode(userInsertDTO.getPassword()));
		entity = userRepository.save(entity); // reposity.save() returns a reference to object saved in DB
		return EntityToDTO(entity);
	}

	@Transactional
	public UserDTO update(Long id, UserInsertUpdateDTO userInsertDTO) {
		Optional<User> obj = userRepository.findById(id);
		User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Error. Id not found: " + id));
		entity = DTOToEntity(userInsertDTO);
		entity.setId(id);
		entity.setPassword(pwdEncoder.encode(userInsertDTO.getPassword()));
		entity = userRepository.save(entity);
		return EntityToDTO(entity);

	}

	public void delete(Long id) {
		try {
			userRepository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Error. Id not found: " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Error. Integrity violation: " + id);
		}

	}

	private UserDTO EntityToDTO(User entity) {
		UserDTO userDTO = modelMapper.map(entity, UserDTO.class);
		Set<RoleDTO> roleDTOSet = entity.getRoles().stream().map(element -> modelMapper.map(element, RoleDTO.class))
				.collect(Collectors.toSet());
		userDTO.getRoles().clear();
		for (RoleDTO roleItem : roleDTOSet) {
			userDTO.getRoles().add(roleItem);
		}
		return userDTO;
	}

	private User DTOToEntity(UserDTO userDTO) {
		User user = modelMapper.map(userDTO, User.class);
		user.getRoles().clear();
		for (RoleDTO roleDtoItem : userDTO.getRoles()) {
			try {
				Role role = roleRepository.getReferenceById(roleDtoItem.getId());
				user.getRoles().add(role);
			} catch (EntityNotFoundException e) {
				throw new NestedResourceNotFoundException("Error. Role id not found : " + roleDtoItem.getId());
			}
		}
		return user;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username);
		if (user == null) {
			logger.error("User not found: " + username);
			throw new UsernameNotFoundException("User not found: " + username);
		}
		logger.info("User found: " + username);
		return user;
	}

}
