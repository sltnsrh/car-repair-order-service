package com.salatin.orderservice.repository;

import com.salatin.orderservice.model.Order;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {

    Flux<Order> findAllByCarIdAndStatusNot(String carId, String status);
}
