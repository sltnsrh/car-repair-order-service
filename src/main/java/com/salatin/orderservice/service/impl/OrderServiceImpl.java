package com.salatin.orderservice.service.impl;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.repository.OrderRepository;
import com.salatin.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public Mono<Order> save(Order order) {
        return orderRepository.save(order).log();
    }

    @Override
    public Mono<Void> delete(Order order) {
        return orderRepository.delete(order).log();
    }

    @Override
    public Mono<Order> findById(String id) {
        return orderRepository.findById(id).log();
    }
}
