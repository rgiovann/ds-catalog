package com.devsuperior.dscatalog.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

// this annotation register this class as a component 
// that will be part of the dependency injection system

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository repository;
	
	// ACID properties
	@Transactional(readOnly = true)
	public Page<CategoryDTO> findAllPaged(Pageable pageable) {
		Page<Category> list = repository.findAll(pageable);
		// Page already is an stream since Java 8.X, noo need to convert
		return list.map(p -> new CategoryDTO(p));

	}

	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {
		Optional<Category> obj = repository.findById(id);
		Category entity = obj.orElseThrow(() -> new ResourceNotFoundException("Error. Id not found: " + id));
		return new CategoryDTO(entity);
	}

	@Transactional
	public CategoryDTO insert(CategoryDTO categoryDTO) {
		Category entity = new Category();
		entity.setName(categoryDTO.getName());
		entity = repository.save(entity); // reposity.save() returns a reference to object saved in DB
		return new CategoryDTO(entity);
	}

	@Transactional
	public CategoryDTO update(Long id, CategoryDTO categoryDTO) {
		try {
			// getReferenceById 
			Category proxyEntity = repository.getReferenceById(id);
			proxyEntity.setName(categoryDTO.getName());
			/*
			 * -- https://www.baeldung.com/jpa-entity-manager-get-reference --
			 * Surprisingly, the result of the running test method is still the same and 
			 * we see the SELECT query remains. As we can see, Hibernate does execute a 
			 * SELECT query when we use getReference() to update an entity field.
			 * Therefore, using the getReference() method does not avoid the extra SELECT
			 * query if we execute any setter of the entity proxy's fields.
			 */
			
			/*
			 *  -- https://vladmihalcea.com/jpa-persist-and-merge/ --
			 *  The save method serves no purpose. Even if we remove it, Hibernate will still
			 *   issue the UPDATE statement since the entity is managed and any state change 
			 *   is propagated as long as the currently running EntityManager is open.
			 * 
			 */
			proxyEntity = repository.save(proxyEntity);			
			return new CategoryDTO(proxyEntity);
			
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Error. Id not found : " + id);
		}
	}

	/*
	 * Persisting and deleting objects in JPA requires a transaction. That's why we should use 
	 * a @Transactional annotation when using these derived delete queries, 
	 * to make sure a transaction is running. 
	 * This is explained in detail in the ORM with Spring documentation.
	 * 
	 */
	//@Transactional
	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Error. Id not found: " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Error. Integrity violation: " + id);
		}

	}
}
