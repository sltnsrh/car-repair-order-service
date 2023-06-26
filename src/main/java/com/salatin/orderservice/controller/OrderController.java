package com.salatin.orderservice.controller;

import com.salatin.orderservice.model.dto.request.OrderCreateRequestDto;
import com.salatin.orderservice.model.dto.response.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;


    @PostMapping
    public Mono<OrderResponseDto> create(@RequestBody OrderCreateRequestDto requestDto) {

        return orderService.create(requestDto);
    }
}
