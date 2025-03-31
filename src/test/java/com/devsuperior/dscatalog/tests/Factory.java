package com.devsuperior.dscatalog.tests;

import java.time.Instant;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;

public class Factory {

 

	public static Product createProduct() {
		Product product = new Product(1L, "This is a cellphone",
				"A very good phone. Indeed a very good but no sheap cellphone", 90.5,
				"https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/Big_picture_of_cellphone.jpg",
				Instant.parse("2020-07-13T20:50:07Z"));
		product.getCategories().add(createGoodCategory());
		return product;
	}

	public static ProductDTO createProductDTO()
	{
		Product product = createProduct();
		ProductDTO productDTO = new ProductDTO();
		productDTO.setId(product.getId());
		productDTO.setName(product.getName());
		productDTO.setDescription(product.getDescription());
		productDTO.setPrice(product.getPrice());
		productDTO.setImgUrl(product.getImgUrl());
		productDTO.setDate(product.getDate());	
		return productDTO;
	}
	
	public static CategoryDTO createGoodDTOCategory() {
		return new CategoryDTO(2L, "Electronics");
	}
	
	public static Category createGoodCategory() {
		return new Category(2L, "Electronics");
	}

	public static CategoryDTO createBadDTOCategory() {
		return new CategoryDTO(200L, "");
	}
	
}
