package com.salatin.orderservice.service;

public interface KafkaProducer<T> {

    void send(String objectId, T message);
}
