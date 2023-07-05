package com.salatin.orderservice.controller;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.dto.request.OrderCreateRequestDto;
import com.salatin.orderservice.model.dto.response.OrderResponseDto;
import com.salatin.orderservice.service.OrderManagementService;
import com.salatin.orderservice.service.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Orders management")
public class OrderController {
    private final OrderManagementService orderManagementService;
    private final OrderMapper orderMapper;

    @Operation(
        summary = "Create a new order",
        description = "Creates a new order by a customer or by a manager "
            + "if there is no opened order already")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created a new order"),
        @ApiResponse(responseCode = "400", description = "Car id or complaints field is empty"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Can't find a car with id"),
        @ApiResponse(responseCode = "409", description = "Car is already in a process of repairing")
    })
    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize(value = "hasAnyRole('manager', 'customer')")
    public Mono<OrderResponseDto> create(@RequestBody @Valid
                                         OrderCreateRequestDto requestDto,
                                         @AuthenticationPrincipal
                                         JwtAuthenticationToken authenticationToken) {
        Order order = orderMapper.toModel(requestDto);

        return orderManagementService.create(order, authenticationToken)
            .map(orderMapper::toDto);
    }

    @Operation(
        summary = "Cancel the order",
        description = "Allows customer or manager to cancel created order "
            + "if repairing haven't done yet"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Canceled successfully"),
        @ApiResponse(responseCode = "202", description = "Current orders status doesn't allow to cancel it"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Can't find an order with id")
    })
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize(value = "hasAnyRole('manager', 'customer')")
    public Mono<OrderResponseDto> cancel(@PathVariable(value = "orderId") String orderId,
                                         @AuthenticationPrincipal JwtAuthenticationToken authenticationToken) {

        return orderManagementService.cancel(orderId, authenticationToken)
            .map(orderMapper::toDto);
    }

    @Operation(
        summary = "Find the order",
        description = "Allows to find particular order by id"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Can't find an order with id")
    })
    @GetMapping("/{orderId}")
    @PreAuthorize(value = "hasAnyRole('manager', 'customer', 'mechanic')")
    public Mono<OrderResponseDto> findById(@PathVariable String orderId) {
        return orderManagementService.getById(orderId)
            .map(orderMapper::toDto);
    }
}
