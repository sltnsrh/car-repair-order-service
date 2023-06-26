package com.salatin.orderservice.service;

import com.salatin.orderservice.model.Order;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<Order> create(Order order);

    Mono<Void> delete(Order order);

    Mono<Order> findById(String id);
}
