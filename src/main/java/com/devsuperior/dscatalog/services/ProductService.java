package com.devsuperior.dscatalog.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.NestedResourceNotFoundException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

// this annotation register this class as a component 
// that will be part of the dependency injection system

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;
	
    @Autowired
    private ModelMapper modelMapper;

	// ACID properties
//	@Transactional(readOnly = true)
//	public Page<ProductDTO> findAllPaged(Pageable pageRequest) {
//		Page<Product> list = repository.findAll(pageRequest);
//		// Page already is an stream since Java 8.X, noo need to convert
//		return list.map(p -> new ProductDTO(p));
//
//	}
    
    //****************************************
    // Now using modelMapper model
    //****************************************
    
	//ACID properties
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Long categoryId, String name, Pageable pageRequest) {
		List<Category> categories = (categoryId == 0) ? null : Arrays.asList(categoryRepository.getReferenceById(categoryId));
		Page<Product> page = productRepository.findProductsWithFilter(categories,name, pageRequest);
		productRepository.findProductsWithCategories(page.getContent()); // chamada auxiliar para buscar as categorias (problema N+1)
		return page.map(p -> {ProductDTO pDTO = modelMapper.map(p, ProductDTO.class); 
										 pDTO.setCategoriesProductDTO(p.getCategories()); 
										 return pDTO;
							 }
		                );

	}
    

//	@Transactional(readOnly = true)
//	public ProductDTO findById(Long id) {
//		Optional<Product> obj = repository.findById(id);
//		Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Error. Id not found: " + id));
//		return new ProductDTO(entity, entity.getCategories());
//	}
	
    //****************************************
    // Now using modelMapper model
    //****************************************
	
	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = productRepository.findById(id);
		Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Error. Id not found: " + id));
		ProductDTO productDTO = modelMapper.map(entity, ProductDTO.class);
		productDTO.setCategoriesProductDTO(entity.getCategories());
		return productDTO;
	}
	
    //****************************************
    // Now using modelMapper model
    //****************************************
	
	@Transactional
	public ProductDTO insert(ProductDTO productDTO) {
		Product entity = new Product();
		entity = modelMapper.map(productDTO, Product.class);
		copyDtoToEntity(productDTO, entity);
		entity = productRepository.save(entity); // reposity.save() returns a reference to object saved in DB
		return modelMapper.map(entity, ProductDTO.class);
	}

    //****************************************
    // Now using modelMapper model
    //****************************************
	
	@Transactional
	public ProductDTO update(Long id, ProductDTO productDTO) {

			Optional<Product> obj = productRepository.findById(id);
			Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Error. Id not found: " + id));
			
			entity = modelMapper.map(productDTO, Product.class);
			entity.setId(id);
					
			copyDtoToEntity(productDTO, entity);
			
			//Product proxyEntity = repository.getReferenceById(id);
			
			/*
			 * -- https://www.baeldung.com/jpa-entity-manager-get-reference -- Surprisingly,
			 * the result of the running test method is still the same and we see the SELECT
			 * query remains. As we can see, Hibernate does execute a SELECT query when we
			 * use getReference() to update an entity field. Therefore, using the
			 * getReference() method does not avoid the extra SELECT query if we execute any
			 * setter of the entity proxy's fields.
			 */

			/*
			 * -- https://vladmihalcea.com/jpa-persist-and-merge/ -- The save method serves
			 * no purpose. Even if we remove it, Hibernate will still issue the UPDATE
			 * statement since the entity is managed and any state change is propagated as
			 * long as the currently running EntityManager is open.
			 * 
			 */

			entity = productRepository.save(entity);
			return modelMapper.map(entity, ProductDTO.class);
 
	}

	/*
	 * Persisting and deleting objects in JPA requires a transaction. That's why we
	 * should use a @Transactional annotation when using these derived delete
	 * queries, to make sure a transaction is running. This is explained in detail
	 * in the ORM with Spring documentation.
	 * 
	 */
	//@Transactional
	public void delete(Long id) {
		try {
			productRepository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Error. Id not found: " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Error. Integrity violation: " + id);
		}

	}

	private void copyDtoToEntity(ProductDTO dto, Product entity) {

		for (CategoryDTO catDTO : dto.getCategories()) {
			try {
				Category category = categoryRepository.getReferenceById(catDTO.getId());
				entity.getCategories().add(category);
			} catch (EntityNotFoundException e) {
				throw new NestedResourceNotFoundException("Error. Category id not found : " + catDTO.getId());
			}
		}
	}
}
