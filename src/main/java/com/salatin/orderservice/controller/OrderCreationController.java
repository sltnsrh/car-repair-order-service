package com.salatin.orderservice.controller;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.dto.request.OrderCreateRequestDto;
import com.salatin.orderservice.model.dto.response.OrderResponseDto;
import com.salatin.orderservice.service.OrderManagementService;
import com.salatin.orderservice.service.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderCreationController {
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
}
