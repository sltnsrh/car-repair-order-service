package com.salatin.orderservice.service;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.util.OrderResponseCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderRetrievalService {
    private final OrderService orderService;

    public Mono<Order> getById(String orderId) {
        return findByIdOrError(orderId);
    }

    public Mono<Order> findByIdOrError(String orderId) {
        return orderService.findById(orderId)
                .switchIfEmpty(Mono.error(() ->
                        OrderResponseCreator.createOrderNotFoundException(orderId)));
    }
}
