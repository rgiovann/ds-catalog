package com.devsuperior.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.tests.Factory;

@DataJpaTest
public class ProductRepositoryTests {
	
	private long existingId;
	private long nonExistingId;
	private long countTotalProducts;


	@Autowired
	private ProductRepository repository;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId =1L;
		nonExistingId = 999L;
		countTotalProducts = 25;
	}

    @DisplayName("001 - delete action should delete object when Id exists.")
	@Test
	public void deleteShouldDeleteObjectWhenIdExists() {

		repository.deleteById(existingId);

		// if object not present BEFORE delete, will throw exception
		Optional<Product> result = repository.findById(existingId);
		Assertions.assertFalse(result.isPresent());

	}

    @DisplayName("002 - delete action should throw EmptyResultDataAccessException when Id does not exists.")
	@Test
	public void deleteShouldThrowEmptyResultDataAccessExceptionWhenObjectDoesNotIdExists() {
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			repository.deleteById(nonExistingId);
		});

	}
    
    @DisplayName("003 - save action should persist object whith autoincrement when Id is null.")
	@Test
	public void saveShouldPersisObjecttWithAutoincrementWhenIdIsNull() {
    	Product product = Factory.createProduct();
    	product.setId(null);
    	product = repository.save(product);
    	Assertions.assertNotNull(product.getId());
    	Assertions.assertEquals(countTotalProducts+1, product.getId());
    	
    }
    
    @DisplayName("004 - findById action should return a non empty Optional<Product> when Id exists.")
	@Test
	public void findByIdShouldReturNonEmptyOptionalWithThisIdWhenIdIsNotNull() {
    	Optional<Product> result = repository.findById(existingId);
    	Assertions.assertTrue(result.isPresent());
    	Assertions.assertEquals(existingId, result.get().getId());
    	
    }
    
    @DisplayName("005 - findById action should return an empty Optional<Product> when Id does not exists.")
	@Test
	public void findByIdShouldReturnEmptyOptionaldWhenIdIsDoesNotExists() {
    	Optional<Product> result = repository.findById(nonExistingId);
    	Assertions.assertTrue(result.isEmpty());
     	
    }

}
