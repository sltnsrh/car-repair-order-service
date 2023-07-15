package com.salatin.orderservice.service;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import com.salatin.orderservice.util.RoleChecker;
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

    public Mono<Order> cancel(String orderId, JwtAuthenticationToken authenticationToken) {
        return findByIdOrError(orderId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Can't find an order by id: " + orderId)))
            .doOnNext(order -> log.info("Retrieved the order: {}", order))
            .flatMap(order -> processCancellation(order, authenticationToken))
            .doOnNext(order -> log.info("Order {} status was changed to {} by the user {}",
                order.getId(), order.getStatus(), authenticationToken.getName()))
            .flatMap(orderService::save)
            .log();
    }

    public Mono<Order> submitNewOrder(String orderId, JwtAuthenticationToken authenticationToken) {
        return getById(orderId)
            .flatMap(order -> {
                if (order.getStatus().equals(OrderStatus.CREATED)) {
                    order.setStatus(OrderStatus.SUBMITTED);
                    order.setSubmittedAt(LocalDateTime.now());
                    order.setManagerId(authenticationToken.getName());
                    return orderService.save(order);
                } else {
                    log.warn("Can't submit order by manager {}. Order is in status {}",
                        authenticationToken.getName(), order.getStatus());

                    return Mono.error(createConflictOrderStatusException(order.getStatus().name()));
                }
            });
    }

    public Mono<Order> acceptReceivingCarByService(String orderId, JwtAuthenticationToken authenticationToken) {
        return getById(orderId)
            .flatMap(order -> {
                if (order.getStatus().equals(OrderStatus.SUBMITTED)) {
                    order.setStatus(OrderStatus.CAR_RECEIVED);
                    order.setCarReceivedAt(LocalDateTime.now());
                    return orderService.save(order);
                } else {
                    log.warn("Can't receive a car by manager {}. Order is in status {}",
                        authenticationToken.getName(), order.getStatus());

                    return Mono.error(createConflictOrderStatusException(order.getStatus().name()));
                }
            });
    }

    public Mono<Order> getById(String orderId) {
        return findByIdOrError(orderId);
    }

    public Mono<Order> startWorkOnOrder(String orderId, JwtAuthenticationToken authenticationToken) {
        return getById(orderId)
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

                    return Mono.error(createConflictOrderStatusException(status.name()));
                }
            });
    }

    public Mono<Order> completeWork(String orderId, JwtAuthenticationToken token) {
        return getById(orderId)
            .flatMap(order -> {
                var status = order.getStatus();
                if (status.equals(OrderStatus.IN_PROGRESS)) {
                    order.setStatus(OrderStatus.COMPLETED);
                    order.setFinishedWorksAt(LocalDateTime.now());
                    return orderService.save(order);
                } else {
                    log.warn("Can't complete work on the car, because order status is {}", status);

                    return Mono.error(createConflictOrderStatusException(status.name()));
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
            .switchIfEmpty(Mono.error(createOrderNotFoundException(orderId)));
    }

    private Mono<Order> findByIdOrError(String orderId) {
        return orderService.findById(orderId)
            .switchIfEmpty(Mono.error(() -> createOrderNotFoundException(orderId)));
    }

    private ResponseStatusException createOrderNotFoundException(String orderId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Can't find an order with id: " + orderId);
    }

    private ResponseStatusException createConflictOrderStatusException(String orderStatus) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
            "You can't do it. Order currently is in status " + orderStatus);
    }

    private Mono<Order> processCancellation(Order order,
                                            JwtAuthenticationToken authenticationToken) {
        OrderStatus orderStatus = order.getStatus();

        if (clientIsManagerAndOrderStatusUnacceptable(authenticationToken, orderStatus)
            || clientIsCustomerAndOrderStatusUnacceptable(authenticationToken, orderStatus)) {

            return Mono.error(new ResponseStatusException(HttpStatus.ACCEPTED,
                "You can't cancel the order because it is already " + order.getStatus()));
        }

        order.setStatus(OrderStatus.CANCELED);
        return Mono.just(order);
    }

    private boolean clientIsManagerAndOrderStatusUnacceptable(JwtAuthenticationToken authenticationToken,
                                                              OrderStatus orderStatus) {
        return RoleChecker.hasRoleManager(authenticationToken)
            && (orderStatus.equals(OrderStatus.PAYED)
            || orderStatus.equals(OrderStatus.COMPLETED)
            || orderStatus.equals(OrderStatus.CANCELED));
    }

    private boolean clientIsCustomerAndOrderStatusUnacceptable(JwtAuthenticationToken authenticationToken,
                                                               OrderStatus orderStatus) {
        return !RoleChecker.hasRoleManager(authenticationToken)
                && !orderStatus.equals(OrderStatus.CREATED);
    }
}
