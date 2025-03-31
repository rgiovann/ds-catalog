package com.devsuperior.dscatalog.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@SpringBootTest
@Transactional // do db rollback because test must be order independent
public class ProductServiceIT {

	@Autowired
	private ProductService service;
	
	@Autowired
	private ProductRepository repository;
	
	private long existingId;
	private long nonExistingId;
	private long countTotalProdutcts;
	
	
	@BeforeEach
	void setUp() throws Exception{
		existingId = 1L;
		nonExistingId = 100L;
		countTotalProdutcts = 25L;
	}
	
	@Test
	public void deleteShouldDeleteResourceWhenIdExists() {
		
		service.delete(existingId);
		Assertions.assertEquals(countTotalProdutcts-1, repository.count());
	}
	
	@Test
	public void deleteShouldShouldThrowResourceNotFoundExceptionWheIdDoesNotExists() {
		
 		Assertions.assertThrows(ResourceNotFoundException.class, () -> {service.delete(nonExistingId);});
	}
	
	@Test
	public void findAllPagedShouldShouldReturnPagedWhenPage0Size10() {
		
		PageRequest pageReguest = PageRequest.of(0,10);
 		Page<ProductDTO> result = service.findAllPaged(0L,"",pageReguest);
 		Assertions.assertFalse(result.isEmpty());
 		Assertions.assertEquals(0,result.getNumber());
 		Assertions.assertEquals(10,result.getSize());
 		Assertions.assertEquals(countTotalProdutcts,result.getTotalElements());
	}
	
	@Test
	public void findAllPagedShouldShouldReturnEmptyPagedWhenPageDoesNotExist() {
		
		PageRequest pageReguest = PageRequest.of(50,10);
 		Page<ProductDTO> result = service.findAllPaged(0L,"",pageReguest);
 		Assertions.assertTrue(result.isEmpty());
 		Assertions.assertEquals(50,result.getNumber());
 		Assertions.assertEquals(10,result.getSize());
 		Assertions.assertEquals(countTotalProdutcts,result.getTotalElements());
	}
	
	@Test
	public void findAllPagedShouldShouldReturnSortedPageWhenSortByName() {
		
		PageRequest pageReguest = PageRequest.of(0,10,Sort.by("name"));
 		Page<ProductDTO> result = service.findAllPaged(0L,"",pageReguest);
 		Assertions.assertFalse(result.isEmpty());
 		Assertions.assertEquals(0,result.getNumber());
 		Assertions.assertEquals(10,result.getSize());
 		Assertions.assertEquals(countTotalProdutcts,result.getTotalElements());
 		Assertions.assertEquals("Macbook Pro", result.getContent().get(0).getName());
 		Assertions.assertEquals("PC Gamer", result.getContent().get(1).getName());
 		Assertions.assertEquals("PC Gamer Alfa", result.getContent().get(2).getName());

	}
	
}
