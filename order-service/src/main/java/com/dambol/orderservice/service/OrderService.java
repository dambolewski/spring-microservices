package com.dambol.orderservice.service;

import com.dambol.orderservice.dto.OrderLineItemsDto;
import com.dambol.orderservice.dto.OrderRequest;
import com.dambol.orderservice.model.Order;
import com.dambol.orderservice.model.OrderLineItems;
import com.dambol.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = null;
        if (orderRequest.getOrderLineItemsDtoList() != null) {
            orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
                    .map(this::mapToDto)
                    .toList();
        }
        order.setOrderLineItemsList(orderLineItems);

        orderRepository.save(order);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
