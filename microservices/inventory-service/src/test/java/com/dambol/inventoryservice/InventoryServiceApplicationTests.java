package com.dambol.inventoryservice;

import com.dambol.inventoryservice.model.Inventory;
import com.dambol.inventoryservice.repository.InventoryRepository;
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
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class InventoryServiceApplicationTests {

	@Container
	static MySQLContainer mySQLContainer = new MySQLContainer("mysql:latest")
			.withUsername("root")
			.withPassword("adminadmin");
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private InventoryRepository inventoryRepository;
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
	}

	@Test
	@DirtiesContext
	@Transactional
	public void shouldCheckIfItsInStock() throws Exception {

		Inventory inventory1 = getInventory("iphone_13",100);
		Inventory inventory2 = getInventory("iphone_13_red",0);
		inventoryRepository.save(inventory1);
		inventoryRepository.save(inventory2);
		String content1 = objectMapper.writeValueAsString(inventory1);
		String content2 = objectMapper.writeValueAsString(inventory2);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/iphone_13")
						.contentType(MediaType.APPLICATION_JSON)
						.content(content1))
				.andExpect(MockMvcResultMatchers.status().isOk());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/iphone_13_red")
						.contentType(MediaType.APPLICATION_JSON)
						.content(content2))
				.andExpect(MockMvcResultMatchers.status().isOk());

	}

	private Inventory getInventory(String skuCode, Integer quantity) {
		// Create product
		return Inventory.builder()
				.skuCode(skuCode)
				.quantity(quantity)
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
