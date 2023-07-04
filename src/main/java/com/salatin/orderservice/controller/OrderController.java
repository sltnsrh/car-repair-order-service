package com.salatin.orderservice.controller;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.dto.request.OrderCreateRequestDto;
import com.salatin.orderservice.model.dto.response.OrderResponseDto;
import com.salatin.orderservice.service.OrderManagementService;
import com.salatin.orderservice.service.mapper.OrderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderManagementService orderManagementService;
    private final OrderMapper orderMapper;

    @PostMapping
    @PreAuthorize(value = "hasAnyRole('manager', 'customer')")
    public Mono<OrderResponseDto> create(@RequestBody @Valid OrderCreateRequestDto requestDto,
                                         @AuthenticationPrincipal JwtAuthenticationToken authenticationToken) {
        Order order = orderMapper.toModel(requestDto);

        return orderManagementService.register(order, authenticationToken)
                .map(orderMapper::toDto);
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize(value = "hasAnyRole('manager', 'customer')")
    public Mono<OrderResponseDto> cancel(@PathVariable(value = "orderId") String orderId,
                                         @AuthenticationPrincipal JwtAuthenticationToken authenticationToken) {

        return orderManagementService.cancel(orderId, authenticationToken)
            .map(orderMapper::toDto);
    }
}
