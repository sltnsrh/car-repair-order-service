package com.salatin.orderservice.service;

import com.salatin.orderservice.model.dto.LogMessage;
import com.salatin.orderservice.model.Order;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<Order> save(Order order);

    Mono<Order> findById(String id);

    Flux<Order> findAll(PageRequest pageRequest);

    Flux<Order> findAllByStatus(PageRequest pageRequest, String status);

    Flux<Order> findAllByCarId(String carId);

    Flux<Order> findAllByUser(String userId, PageRequest pageRequest);

    void addLogToOrder(String orderId, LogMessage logMessage);
}
