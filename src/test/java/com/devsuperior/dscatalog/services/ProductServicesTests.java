package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.NestedResourceNotFoundException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServicesTests {

	@InjectMocks
	private ProductService service;

	@Mock
	private ProductRepository productRepository;
	
	@Mock
	private CategoryRepository categoryRepository;
	
	@Mock
    private ModelMapper modelMapper;


	
	private long productExistingId;
	private long productNonExistingId;
	private long productDependentId;
	private PageImpl<Product> page;
	private Product product;
	private ProductDTO productGooDDTO;
	private ProductDTO productBadDTO;
	
	private Category category;
	private long nonExistingCategoryId;
	private long existingCategoryId;

	
	@BeforeEach
	void setUp() throws Exception {
		
		productNonExistingId = 999;   // esses valores não importam pois é simulado pelo Mockito
		productDependentId = 3L;
		
		product = Factory.createProduct();
		
		productGooDDTO = Factory.createProductDTO();
		productGooDDTO.getCategories().add(Factory.createGoodDTOCategory());
		
		page = new PageImpl<Product>(List.of(product));
		existingCategoryId = Factory.createGoodDTOCategory().getId();
		
		
		productBadDTO = Factory.createProductDTO();
		productBadDTO.getCategories().add(Factory.createBadDTOCategory());
		nonExistingCategoryId = Factory.createBadDTOCategory().getId();
		
		productExistingId = productGooDDTO.getId();
		
	
		Mockito.when( productRepository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);
		 
		Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(product);

		Mockito.when(productRepository.findById(productExistingId)).thenReturn(Optional.of(product));
		
		Mockito.when(productRepository.findById(productNonExistingId)).thenReturn(Optional.empty());
		
		// change test for filters (customized queries)
		Mockito.when(productRepository.findProductsWithFilter(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(page);

		
		Mockito.when(productRepository.getReferenceById(productExistingId)).thenReturn(product);
		
		Mockito.when(productRepository.getReferenceById(productNonExistingId)).thenThrow(EntityNotFoundException.class);
		
		Mockito.when(categoryRepository.getReferenceById(existingCategoryId)).thenReturn(category);	
 		
		Mockito.when(categoryRepository.getReferenceById(nonExistingCategoryId)).thenThrow(NestedResourceNotFoundException.class);
			
		Mockito.doNothing().when(productRepository).deleteById(productExistingId);
		
		Mockito.doThrow(ResourceNotFoundException.class).when(productRepository).deleteById(productNonExistingId);
		
		Mockito.doThrow(DatabaseException.class).when(productRepository).deleteById(productDependentId);
		
		Mockito.when(modelMapper.map(ArgumentMatchers.any(Product.class), ArgumentMatchers.eq(ProductDTO.class))).thenReturn(productGooDDTO);
		
		Mockito.when(modelMapper.map(ArgumentMatchers.any(ProductDTO.class), ArgumentMatchers.eq(Product.class))).thenReturn(product);


	}
	
	
    @DisplayName("001 - update should thrown ResourceNotFound exception when product id does not exist.")
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		
 		Assertions.assertThrows(ResourceNotFoundException.class,() -> {service.update(productNonExistingId,productGooDDTO);});

		Mockito.verify(productRepository,Mockito.times(1)).findById(productNonExistingId);
		
	}
	
    
    @DisplayName("002 - update should return ProductDTO when product id does exists.")
	@Test
	public void updateShouldReturnProductDTOWhenIdExists() {
    			
		ProductDTO result = service.update(productExistingId,productGooDDTO);
		
		Assertions.assertNotNull(result);
		
		Mockito.verify(productRepository,Mockito.times(1)).findById(productExistingId);
		
		Mockito.verify(productRepository,Mockito.times(1)).save(ArgumentMatchers.any());
		
		Mockito.verify(categoryRepository,Mockito.times(1)).getReferenceById(ArgumentMatchers.any());

	}
	
		
    @DisplayName("003 - findById should return ProductDTO when product id exists.")	
	@Test
	public void findByIdShouldReturnProductDTOWhenIdExists() {
		
		ProductDTO result = service.findById(productExistingId);
		
		Assertions.assertNotNull(result);
		
		Mockito.verify(productRepository,Mockito.times(1)).findById(productExistingId);
		
	}	
    
    @DisplayName("004 - findById should thrown ResourceNotFound exception when product id does not exist.")
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		
 		Assertions.assertThrows(ResourceNotFoundException.class,() -> {service.findById(productNonExistingId);});

		Mockito.verify(productRepository,Mockito.times(1)).findById(productNonExistingId);
		
	}
	
    @DisplayName("005 - delete should do nothing when product id does exists.")    
	@Test
	public void deleteShouldDoNotingWhenIdExists() {
		
		Assertions.assertDoesNotThrow(() -> {service.delete(productExistingId);});
		
		Mockito.verify(productRepository,Mockito.times(1)).deleteById(productExistingId);
		
	}
	
    @DisplayName("006 - delete should thrown ResourceNotFound exception when product id does not exist.")        
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		
		Assertions.assertThrows(ResourceNotFoundException.class,() -> {service.delete(productNonExistingId);});
		
		Mockito.verify(productRepository,Mockito.times(1)).deleteById(productNonExistingId);
		
	}
	
    @DisplayName("007 - delete should thrown DatabaseException exception when product id has dependencies.")        
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		
		Assertions.assertThrows(DatabaseException.class,() -> {service.delete(productDependentId);});
		
		Mockito.verify(productRepository,Mockito.times(1)).deleteById(productDependentId);
		
	}
	
    @DisplayName("008 - findAll should return Page.")        
	@Test
	public void findAllPagedShouldReturnPage() {
		
		Pageable pageable = PageRequest.of(0,10);
		
		Page<ProductDTO> result = service.findAllPaged(0L, "",pageable);
		
		Assertions.assertNotNull(result);
		
		Mockito.verify(productRepository,Mockito.times(1)).findProductsWithFilter(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any());
		
	}
	
    
    @DisplayName("009 - insert should return ProductDTO")
	@Test
	public void insertShouldReturnProductDTO() {
		
		ProductDTO result = service.insert(productGooDDTO);
		
		Assertions.assertNotNull(result);
		
		Mockito.verify(productRepository,Mockito.times(1)).save(ArgumentMatchers.any());
		
		Mockito.verify(categoryRepository,Mockito.times(1)).getReferenceById(existingCategoryId);


	}
    
    @DisplayName("010 - update should thrown NestedResourceNotFoundException exception when category id does not exist.")
	@Test
	public void updateShouldThrowNestedResourceNotFoundExceptionWhenCategoryIdDoesNotExists() {
    		
 		Assertions.assertThrows(NestedResourceNotFoundException.class,() -> {service.update(productExistingId,productBadDTO);});

		Mockito.verify(productRepository,Mockito.times(1)).findById(productExistingId);
		
		Mockito.verify(categoryRepository,Mockito.times(1)).getReferenceById(nonExistingCategoryId);

	}   
    
    
    @DisplayName("011 - insert should thrown NestedResourceNotFoundException exception when category id does not exist.")
	@Test
	public void insertShouldTShouldThrowNestedResourceNotFoundExceptionWhenCategoryIdDoesNotExists() {
		
 		Assertions.assertThrows(NestedResourceNotFoundException.class,() -> {service.insert(productBadDTO);});
			
		Mockito.verify(categoryRepository,Mockito.times(1)).getReferenceById(nonExistingCategoryId);


	}
	
}
