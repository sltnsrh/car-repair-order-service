package com.salatin.orderservice.service;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<Order> save(Order order);

    Mono<Void> delete(Order order);

    Mono<Order> findById(String id);

    Flux<Order> findByCarIdAndStatus(String carId, String status);
}
