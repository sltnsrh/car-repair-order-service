package com.salatin.orderservice.controller;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.dto.request.OrderCreateRequestDto;
import com.salatin.orderservice.model.dto.response.OrderResponseDto;
import com.salatin.orderservice.service.OrderRegistrationService;
import com.salatin.orderservice.service.mapper.OrderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderRegistrationService orderRegistrationService;
    private final OrderMapper orderMapper;

    @PostMapping
    public Mono<OrderResponseDto> create(@RequestBody @Valid OrderCreateRequestDto requestDto,
                                         @AuthenticationPrincipal JwtAuthenticationToken authenticationToken) {
        Order order = orderMapper.toModel(requestDto);

        return orderRegistrationService.register(order, authenticationToken)
                .map(orderMapper::toDto);
    }
}
