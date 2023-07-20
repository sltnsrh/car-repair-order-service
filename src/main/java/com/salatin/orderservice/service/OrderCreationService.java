package com.salatin.orderservice.service;

import com.salatin.orderservice.model.dto.Car;
import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import com.salatin.orderservice.util.RoleChecker;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderCreationService {
    private final OrderService orderService;
    private final WebClient.Builder webClientBuilder;

    public Mono<Order> create(Order order, JwtAuthenticationToken authentication) {
        var bearerToken = "Bearer " + authentication.getToken().getTokenValue();
        var carId = order.getCarId();

        return checkIfCarHasNotOpenedOrders(carId)
                .then(getCarFromCarService(carId, bearerToken))
                .flatMap(car -> registerOrder(order, car, authentication));
    }

    private Mono<Void> checkIfCarHasNotOpenedOrders(String carId) {
        return orderService.findAllByCarId(carId)
                .filter(order -> !order.getStatus().equals(OrderStatus.PAYED)
                        && !order.getStatus().equals(OrderStatus.CANCELED))
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

    private Mono<Car> getCarFromCarService(String carId, String bearerToken) {
        return webClientBuilder.build()
                .get()
                .uri("http://car-service/cars/{carId}", carId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE, "Car service temporarily unavailable")))
                .bodyToMono(Car.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Can't find a car with id: " + carId)))
                .doOnNext(car -> log.info("Retrieved the car: {}", car));
    }

    private Mono<Order> registerOrder(Order order, Car car, JwtAuthenticationToken authentication) {
        order.setCustomerId(car.getOwnerId());

        if (RoleChecker.hasRoleManager(authentication)) {
            return registerAsManager(order, authentication.getName());
        } else {
            return registerAsCustomer(order, authentication.getName());
        }
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
