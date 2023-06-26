package com.salatin.orderservice.service;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Log4j2
public class OrderRegistrationService {
    private final OrderService orderService;

    public Mono<Order> register(@NotNull Order order,
                                @NotNull JwtAuthenticationToken authenticationToken) {
        var hasRoleManager = authenticationToken.getAuthorities().stream()
                .anyMatch(a -> "ROLE_manager".equals(a.getAuthority()));

        if (hasRoleManager) {
            return registerAsManager(order, authenticationToken.getName());
        }

        return registerAsCustomer(order, authenticationToken.getName());
    }

    private Mono<Order> registerAsManager(Order order, String managerId) {
        if (order.getCustomerId() == null) {
            log.warn("Customer id is null. Field 'customerId' is required");
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Customer id can't be null"));
        }

        order.setStatus(OrderStatus.SUBMITTED);
        order.setManagerId(managerId);
        order.setSubmittedAt(LocalDateTime.now());

        return orderService.save(order);
    }

    private Mono<Order> registerAsCustomer(Order order, String customerId) {
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.CREATED);

        return orderService.save(order);
    }
}
