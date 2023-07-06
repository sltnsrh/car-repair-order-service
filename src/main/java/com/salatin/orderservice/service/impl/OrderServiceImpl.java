package com.salatin.orderservice.service.impl;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import com.salatin.orderservice.repository.OrderRepository;
import com.salatin.orderservice.service.OrderService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public Mono<Order> save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Mono<Order> findById(String id) {
        return orderRepository.findById(id);
    }

    @Override
    public Flux<Order> findAll(PageRequest pageRequest) {
        var firstElement = pageRequest.getPageNumber() * pageRequest.getPageSize();

        return orderRepository.findAll(pageRequest.getSort())
            .skip(firstElement)
            .limitRate(pageRequest.getPageSize());
    }

    @Override
    public Flux<Order> findAllByStatus(PageRequest pageRequest, String status) {
        var firstElement = pageRequest.getPageNumber() * pageRequest.getPageSize();
        var statusOptional = getOrderStatus(status);

        return statusOptional.map(orderStatus -> findAll(pageRequest)
            .filter(order -> order.getStatus().equals(orderStatus))
            .skip(firstElement)
            .limitRate(pageRequest.getPageSize()))

            .orElseGet(() -> Flux.error(
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status value")));
    }

    @Override
    public Flux<Order> findAllByCarId(String carId) {
        return orderRepository.findAllByCarId(carId);
    }

    private Optional<OrderStatus> getOrderStatus(String status) {
        try {
            return Optional.of(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}
