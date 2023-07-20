package com.salatin.orderservice.service.impl;

import com.salatin.orderservice.model.dto.LogMessage;
import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import com.salatin.orderservice.repository.OrderRepository;
import com.salatin.orderservice.service.OrderService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Log4j2
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
        var firstElement = calculateFirstElement(pageRequest);

        return orderRepository.findAll(pageRequest.getSort())
            .skip(firstElement)
            .take(pageRequest.getPageSize());
    }

    @Override
    public Flux<Order> findAllByStatus(PageRequest pageRequest, String status) {
        var firstElement = calculateFirstElement(pageRequest);
        var statusOptional = getOrderStatus(status);

        return statusOptional.map(
            orderStatus -> orderRepository.findAllByStatus(orderStatus, pageRequest.getSort())
            .skip(firstElement)
            .take(pageRequest.getPageSize()))
            .orElseGet(() -> Flux.error(
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status value")));
    }

    @Override
    public Flux<Order> findAllByCarId(String carId) {
        return orderRepository.findAllByCarId(carId);
    }

    @Override
    public Flux<Order> findAllByUser(String userId, PageRequest pageRequest) {
        var firstElement = calculateFirstElement(pageRequest);

        return orderRepository.findAllByCustomerId(userId, pageRequest.getSort())
            .skip(firstElement)
            .take(pageRequest.getPageSize());
    }

    @Override
    public void addLogToOrder(String orderId, LogMessage logMessage) {
        orderRepository.findById(orderId)
                .flatMap(order -> {
                    order.getLogs().add(logMessage);
                    return orderRepository.save(order);
                })
                .doOnSuccess(savedOrder -> log.info("A new message {} saved to order {}",
                        logMessage,  orderId))
                .doOnError(throwable -> log.warn("Failed to save log message to order {}",
                        orderId))
                .subscribe();
    }

    private int calculateFirstElement(PageRequest pageRequest) {
        return pageRequest.getPageNumber() * pageRequest.getPageSize();
    }

    private Optional<OrderStatus> getOrderStatus(String status) {
        try {
            return Optional.of(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
