package com.salatin.orderservice.repository;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.OrderStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {

    Flux<Order> findAllByCarId(String carId);

    Flux<Order> findAllByStatus(OrderStatus status, Sort sort);
}
