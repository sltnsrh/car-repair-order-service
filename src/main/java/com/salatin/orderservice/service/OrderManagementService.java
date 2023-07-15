package com.salatin.orderservice.service;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import com.salatin.orderservice.util.OrderResponseCreator;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
public class OrderManagementService {
    private final OrderService orderService;
    private final OrderRetrievalService orderRetrievalService;

    public Mono<Order> submitNewOrder(String orderId, JwtAuthenticationToken authenticationToken) {
        return orderRetrievalService.findByIdOrError(orderId)
            .flatMap(order -> {
                if (order.getStatus().equals(OrderStatus.CREATED)) {
                    order.setStatus(OrderStatus.SUBMITTED);
                    order.setSubmittedAt(LocalDateTime.now());
                    order.setManagerId(authenticationToken.getName());
                    return orderService.save(order);
                } else {
                    log.warn("Can't submit order by manager {}. Order is in status {}",
                        authenticationToken.getName(), order.getStatus());

                    return Mono.error(
                            OrderResponseCreator.createConflictOrderStatusException(order.getStatus().name()));
                }
            });
    }

    public Mono<Order> acceptReceivingCarByService(String orderId,
                                                   JwtAuthenticationToken authenticationToken) {

        return orderRetrievalService.findByIdOrError(orderId)
            .flatMap(order -> {
                if (order.getStatus().equals(OrderStatus.SUBMITTED)) {
                    order.setStatus(OrderStatus.CAR_RECEIVED);
                    order.setCarReceivedAt(LocalDateTime.now());
                    return orderService.save(order);
                } else {
                    log.warn("Can't receive a car by manager {}. Order is in status {}",
                        authenticationToken.getName(), order.getStatus());

                    return Mono.error(
                            OrderResponseCreator.createConflictOrderStatusException(
                                    order.getStatus().name()));
                }
            });
    }

    public Mono<Order> startWorkOnOrder(String orderId, JwtAuthenticationToken authenticationToken) {
        return orderRetrievalService.findByIdOrError(orderId)
            .flatMap(order -> {
                var status = order.getStatus();
                if (status.equals(OrderStatus.CAR_RECEIVED)) {
                    order.setStatus(OrderStatus.IN_PROGRESS);
                    order.setStartedWorksAt(LocalDateTime.now());
                    order.setMechanicId(authenticationToken.getName());
                    return orderService.save(order);
                } else if (status.equals(OrderStatus.SUSPENDED)) {
                    order.setStatus(OrderStatus.IN_PROGRESS);
                    return orderService.save(order);
                } else {
                    log.warn("Can't start working on car, because order is in status {}", status);

                    return Mono.error(
                            OrderResponseCreator.createConflictOrderStatusException(status.name()));
                }
            });
    }

    public Mono<Order> completeWork(String orderId, JwtAuthenticationToken token) {
        return orderRetrievalService.findByIdOrError(orderId)
            .flatMap(order -> {
                var status = order.getStatus();
                if (status.equals(OrderStatus.IN_PROGRESS)) {
                    order.setStatus(OrderStatus.COMPLETED);
                    order.setFinishedWorksAt(LocalDateTime.now());
                    return orderService.save(order);
                } else {
                    log.warn("Can't complete work on the car, because order status is {}", status);

                    return Mono.error(
                            OrderResponseCreator.createConflictOrderStatusException(status.name()));
                }
            });
    }

    public Mono<Order> updateStatus(String orderId, String status) {
        return orderService.findById(orderId)
            .flatMap(order -> {
                order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
                return orderService.save(order);
            })
            .onErrorMap(throwable -> {
                throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, throwable.getMessage());
            })
            .switchIfEmpty(Mono.error(OrderResponseCreator.createOrderNotFoundException(orderId)));
    }
}
