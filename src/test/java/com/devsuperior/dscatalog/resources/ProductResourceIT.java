package com.devsuperior.dscatalog.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.tests.Factory;
import com.devsuperior.dscatalog.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductResourceIT {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private TokenUtil tokenUtil;

	private String userName;
	private String password;
 
	private long countTotalProdutcts;
	//private PageImpl<ProductDTO> page;
	private ProductDTO productDTO;
	
	private long existingId;
	private long nonExistingId;
	//private long dependentId;
	private long productBadCategoryId;
	private String jsonBody;
	//private String jsonBadBody;
	private ProductDTO productBadDTO;

	@BeforeEach
	void setUp() throws Exception {
		userName ="maria@gmail.com";
		password = "123456";
		productDTO = Factory.createProductDTO();
		productDTO.getCategories().add(Factory.createGoodDTOCategory());
		existingId = productDTO.getId();
		nonExistingId = 100L;
		//dependentId = 50L;
		productBadDTO = Factory.createProductDTO();
		productBadDTO.getCategories().add(Factory.createBadDTOCategory());
		productBadDTO.setId(productBadCategoryId);
		productBadCategoryId = Factory.createBadDTOCategory().getId();
		countTotalProdutcts = 25L;
		jsonBody = objectMapper.writeValueAsString(productDTO);
		//jsonBadBody = objectMapper.writeValueAsString(productBadDTO);
	}

    @DisplayName("001 - update should return HttpStatus.OK (200) when id exists..")	
	@Test
	public void findAllShouldReturnSortedPageWhenSortedByName() throws Exception{
		ResultActions result = mockMvc.perform(get("/products?page=0&size=12&sort=name,asc").accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.totalElements").value(countTotalProdutcts));
		result.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
		result.andExpect(jsonPath("$.content[1].name").value("PC Gamer"));
		result.andExpect(jsonPath("$.content[2].name").value("PC Gamer Alfa"));

	}
	
    @DisplayName("002 - update should return HttpStatus.OK (200) when id exists.")	
	@Test
	public void updateIdShouldReturnProducDTOtWhenIdExists() throws Exception {

    	// added token acess 
    	String accessToken = tokenUtil.obtainAccessToken(mockMvc, userName, password);
    	
    	String expectedName = productDTO.getName();
    	String expectedDescription = productDTO.getDescription();
    	
		ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).
				accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").value(expectedName));
		result.andExpect(jsonPath("$.description").value(expectedDescription));

	}
    
    @DisplayName("003 - update should return HttpStatus.NOT_FOUND (404) when id does not exists.")	
	@Test
	public void updateShouldReturnNotFoundtWhenIdDoesNotExists() throws Exception {
    	// added token acess 
    	String accessToken = tokenUtil.obtainAccessToken(mockMvc, userName, password);
    	
		ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId)
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}
    
    @DisplayName("004 - update should return HttpStatus.NOT_FOUND (404) when category id does not exists.")	
	@Test
	public void updateShouldReturnNotFoundtWhenCategoryIdDoesNotExists() throws Exception {

		String jsonBody = objectMapper.writeValueAsString(productDTO);
    	// added token acess 
    	String accessToken = tokenUtil.obtainAccessToken(mockMvc, userName, password);

		ResultActions result = mockMvc.perform(put("/products/{id}", productBadCategoryId)
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}
    
}
