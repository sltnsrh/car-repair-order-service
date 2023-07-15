package com.salatin.orderservice.service;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import com.salatin.orderservice.util.RoleChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderCancellationService {
    private final OrderRetrievalService orderRetrievalService;
    private final OrderService orderService;

    public Mono<Order> cancel(String orderId, JwtAuthenticationToken authenticationToken) {
        return orderRetrievalService.findByIdOrError(orderId)
                .doOnNext(order -> log.info("Retrieved the order: {}", order))
                .flatMap(order -> processCancellation(order, authenticationToken))
                .doOnNext(order -> log.info("Order {} status was changed to {} by the user {}",
                        order.getId(), order.getStatus(), authenticationToken.getName()))
                .flatMap(orderService::save)
                .log();
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
