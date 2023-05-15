package com.dambol.orderservice;

import com.dambol.orderservice.dto.OrderLineItemsDto;
import com.dambol.orderservice.dto.OrderRequest;
import com.dambol.orderservice.repository.OrderRepository;

import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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
class OrderServiceApplicationTests {

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:latest")
            .withUsername("root")
            .withPassword("adminadmin");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
    }

    @Test
    @DirtiesContext
    @Transactional
    public void shouldCreateOrder() throws Exception {
        // Create an OrderRequest object with the given data
        OrderLineItemsDto orderLineItemsDto = getOrderLineItemsDto();
        List<OrderLineItemsDto> orderLineItemsDtoList = Collections.singletonList(orderLineItemsDto);
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrderLineItemsDtoList(orderLineItemsDtoList);
        String orderRequestString = objectMapper.writeValueAsString(orderRequest);

        // Call the endpoint to create the order
        mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestString))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        // Verify that the order was saved to the repository
        Assertions.assertEquals(1, orderRepository.findAll().size());

    }

    private OrderLineItemsDto getOrderLineItemsDto() {
        // Create product
        return OrderLineItemsDto.builder()
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(3499))
                .quantity(1)
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
