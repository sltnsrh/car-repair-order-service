package com.salatin.orderservice.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class OrderResponseCreator {

    private OrderResponseCreator() {}

    public static ResponseStatusException createOrderNotFoundException(String orderId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Can't find an order with id: " + orderId);
    }

    public static ResponseStatusException createConflictOrderStatusException(String orderStatus) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "You can't do it. Order currently is in status " + orderStatus);
    }
}
