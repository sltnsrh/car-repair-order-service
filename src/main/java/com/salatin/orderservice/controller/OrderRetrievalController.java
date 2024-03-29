package com.salatin.orderservice.controller;

import com.salatin.orderservice.model.dto.response.OrderResponseDto;
import com.salatin.orderservice.service.OrderRetrievalService;
import com.salatin.orderservice.service.OrderService;
import com.salatin.orderservice.service.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Orders management")
public class OrderRetrievalController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final OrderRetrievalService orderRetrievalService;

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
        return orderRetrievalService.findByIdOrError(orderId)
            .map(orderMapper::toDto);
    }

    @Operation(
        summary = "Find all orders",
        description = "Retrieving all orders from DB with pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize(value = "hasAnyRole('admin', 'manager', 'mechanic')")
    public Flux<OrderResponseDto> findAll(@RequestParam(defaultValue = "0") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(defaultValue = "createdAt") String sortByField,
                                                  @RequestParam(defaultValue = "ASC") String direction) {
        PageRequest pageRequest = buildPageRequest(page, size, sortByField, direction);

        return orderService.findAll(pageRequest)
            .map(orderMapper::toDto);
    }

    @Operation(
        summary = "Find all orders by status",
        description = "Retrieving all orders by their status from DB with pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/by-status")
    @PreAuthorize(value = "hasAnyRole('admin', 'manager', 'mechanic')")
    public Flux<OrderResponseDto> findAllByStatus(@RequestParam(defaultValue = "0") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(defaultValue = "createdAt") String sortByField,
                                          @RequestParam(defaultValue = "ASC") String direction,
                                          @RequestParam String status) {
        PageRequest pageRequest = buildPageRequest(page, size, sortByField, direction);

        return orderService.findAllByStatus(pageRequest, status)
            .map(orderMapper::toDto);
    }

    @Operation(
        summary = "Find all by customer",
        description = "Retrieving all orders by customer id from DB with pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/customer/{userId}")
    @PreAuthorize(value = "hasAnyRole('admin', 'manager', 'customer')")
    public Flux<OrderResponseDto> findAllByCustomer(@PathVariable String userId,
                                                @RequestParam(defaultValue = "0") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size
                                                ) {
        var pageRequest = buildPageRequest(page, size, "createdAt", "DESC");

        return orderService.findAllByUser(userId, pageRequest)
            .map(orderMapper::toDto);
    }

    private PageRequest buildPageRequest(Integer page,
                                         Integer size,
                                         String sortByField,
                                         String direction) {
        try {
            return PageRequest.of(page, size,
                    Sort.Direction.valueOf(direction.toUpperCase()), sortByField);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
