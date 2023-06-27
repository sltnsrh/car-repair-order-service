package com.salatin.orderservice.service;

import com.salatin.orderservice.model.Car;
import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Log4j2
public class OrderRegistrationService {
    private final OrderService orderService;
    private final WebClient.Builder webClientBuilder;

    public Mono<Order> register(@NotNull Order order,
                                @NotNull JwtAuthenticationToken authenticationToken) {
        var bearerToken = "Bearer " + authenticationToken.getToken().getTokenValue();

        Mono<String> monoCustomerId = webClientBuilder.build().get()
                .uri("http://car-service/cars/{carId}", order.getCarId())
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(Car.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Can't find a car with id: " + order.getCarId())))
                .doOnNext(car -> {
                    String customerId = car.getOwnerId();
                    log.info("Retrieved customerId: {}", customerId);
                })
                .map(Car::getOwnerId);

        var hasRoleManager = authenticationToken.getAuthorities().stream()
                .anyMatch(a -> "ROLE_manager".equals(a.getAuthority()));

        return monoCustomerId.flatMap(customerId -> {
            order.setCustomerId(customerId);

            if (hasRoleManager) {
                return registerAsManager(order, authenticationToken.getName());
            } else {
                return registerAsCustomer(order, authenticationToken.getName());
            }
        });
    }

    private Mono<Order> registerAsManager(Order order, String managerId) {
        order.setStatus(OrderStatus.SUBMITTED);
        order.setManagerId(managerId);
        order.setSubmittedAt(LocalDateTime.now());

        return orderService.save(order);
    }

    private Mono<Order> registerAsCustomer(Order order, String currentUserId) {
        if (order.getCustomerId().equals(currentUserId)) {
            order.setStatus(OrderStatus.CREATED);
            return orderService.save(order);
        }

        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Sorry, you are not owner of this car"));
    }
}
