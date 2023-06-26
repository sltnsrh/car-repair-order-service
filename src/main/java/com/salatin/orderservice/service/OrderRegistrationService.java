package com.salatin.orderservice.service;

import com.salatin.orderservice.model.Order;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderRegistrationService {
    private final OrderService orderService;

    public Mono<Order> register(@NotNull Order order,
                                @NotNull JwtAuthenticationToken authenticationToken) {
        var hasRoleManager = authenticationToken.getAuthorities().stream()
                .anyMatch(a -> "ROLE_manager".equals(a.getAuthority()));

        if (hasRoleManager) {
            if (order.getCustomerId() == null) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Customer id can't be null"));
            }
            return orderService.create(order);
        }

        order.setCustomerId(authenticationToken.getName());
        return orderService.create(order);
    }
}
