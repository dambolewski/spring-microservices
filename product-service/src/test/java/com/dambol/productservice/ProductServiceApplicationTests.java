package com.dambol.productservice;

import com.dambol.productservice.dto.ProductRequest;
import com.dambol.productservice.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
	static MySQLContainer mySQLContainer = new MySQLContainer("mysql:latest")
			.withUsername("root")
			.withPassword("adminadmin");
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ProductRepository productRepository;
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
		dynamicPropertyRegistry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
	}

	@Test
	@DirtiesContext
	@Transactional
	void shouldCreateProduct() throws Exception {
		// Create product
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);

		// Product is created
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(productRequestString))
				.andExpect(MockMvcResultMatchers.status().isCreated());
		Assertions.assertEquals(1, productRepository.findAll().size());
	}

	@Test
	@DirtiesContext
	@Transactional
	void shouldReturnAllProducts() throws Exception {
		// Create some products
		ProductRequest productRequest1 = getProductRequest("iPhone 13", "The latest iPhone", BigDecimal.valueOf(3499));
		String productRequestString1 = objectMapper.writeValueAsString(productRequest1);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(productRequestString1))
				.andExpect(MockMvcResultMatchers.status().isCreated());

		ProductRequest productRequest2 = getProductRequest("Samsung Galaxy S21", "The latest Samsung phone", BigDecimal.valueOf(1999));
		String productRequestString2 = objectMapper.writeValueAsString(productRequest2);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(productRequestString2))
				.andExpect(MockMvcResultMatchers.status().isCreated());

		// Retrieve all products
		mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("iPhone 13"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("The latest iPhone"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].price").value(3499))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Samsung Galaxy S21"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("The latest Samsung phone"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].price").value(1999));
	}

	private ProductRequest getProductRequest() {
		// Create product
		return ProductRequest.builder()
				.name("iPhone 13")
				.description("iPhone 13")
				.price(BigDecimal.valueOf(3499))
				.build();
	}

	private ProductRequest getProductRequest(String name, String desc, BigDecimal price) {
		return ProductRequest.builder()
				.name(name)
				.description(desc)
				.price(price)
				.build();
	}

	@TestConfiguration
	static class Config {

		@Bean
		public ObjectMapper objectMapper() {
			return new ObjectMapper();
		}
	}
}







