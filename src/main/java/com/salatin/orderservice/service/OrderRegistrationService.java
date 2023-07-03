package com.salatin.orderservice.service;

import com.salatin.orderservice.model.Car;
import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
public class OrderRegistrationService {
    private final OrderService orderService;
    private final WebClient.Builder webClientBuilder;

    public Mono<Order> register(@NotNull Order order,
                                @NotNull JwtAuthenticationToken authentication) {
        var bearerToken = "Bearer " + authentication.getToken().getTokenValue();
        var carId = order.getCarId();

        return checkIfCarHasNotOpenedOrders(carId)
            .then(webClientBuilder.build()
                .get()
                .uri("http://car-service/cars/{carId}", carId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                    e -> Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Car-service temporary unavailable")))
                .bodyToMono(Car.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Can't find a car with id: " + carId)))
                .doOnNext(car -> log.info("Retrieved the car: {}", car))
                .map(Car::getOwnerId)
                .flatMap(ownerId -> {
                    order.setCustomerId(ownerId);

                    if (hasRoleManager(authentication)) {
                        return registerAsManager(order, authentication.getName());
                    } else {
                        return registerAsCustomer(order, authentication.getName());
                    }
                }));
    }

    public Mono<Order> cancel(String orderId,
                                         JwtAuthenticationToken authenticationToken) {
        return orderService.findById(orderId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Can't find an order by id: " + orderId)))
            .doOnNext(order -> log.info("Retrieved the order: {}", order))
            .flatMap(order -> {
                var orderStatus = order.getStatus();

                if (clientIsManagerAndOrderStatusUnacceptable(authenticationToken, orderStatus)
                || clientIsCustomerAndOrderStatusUnacceptable(authenticationToken, orderStatus)) {

                    return Mono.error(new ResponseStatusException(HttpStatus.ACCEPTED,
                        "You can't cancel the order, because it is already " + order.getStatus()));
                }
                order.setStatus(OrderStatus.CANCELED);
                log.info("Order {} status was changed to {} by the user {}",
                    order.getId(), order.getStatus(), authenticationToken.getName());

                return orderService.save(order).log();
            });
    }

    private Mono<Void> checkIfCarHasNotOpenedOrders(String carId) {
        return orderService.findByCarIdAndStatusNot(carId, OrderStatus.PAYED.name())
            .collectList()
            .flatMap(orders -> {
                if (orders.isEmpty()) {
                    return Mono.empty();
                } else {
                    return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                            "This car is already in the process of repairing"));
                }
            });
    }

    private boolean hasRoleManager(JwtAuthenticationToken authenticationToken) {
        return authenticationToken.getAuthorities().stream()
                .anyMatch(a -> "ROLE_manager".equals(a.getAuthority()));
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

    private boolean clientIsManagerAndOrderStatusUnacceptable(JwtAuthenticationToken authenticationToken,
                                                              OrderStatus orderStatus) {
        return hasRoleManager(authenticationToken)
            && (orderStatus.equals(OrderStatus.PAYED)
            || orderStatus.equals(OrderStatus.COMPLETED)
            || orderStatus.equals(OrderStatus.CANCELED));
    }

    private boolean clientIsCustomerAndOrderStatusUnacceptable(JwtAuthenticationToken authenticationToken,
                                                               OrderStatus orderStatus) {
        return !hasRoleManager(authenticationToken) && !orderStatus.equals(OrderStatus.CREATED);
    }
}
