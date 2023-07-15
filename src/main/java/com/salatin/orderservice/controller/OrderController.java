package com.salatin.orderservice.controller;

import com.salatin.orderservice.model.LogMessage;
import com.salatin.orderservice.model.dto.response.OrderResponseDto;
import com.salatin.orderservice.service.OrderLogMessageProducer;
import com.salatin.orderservice.service.OrderManagementService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
public class OrderController {
    private final OrderManagementService orderManagementService;
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final OrderLogMessageProducer messageProducer;

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

    @Operation(
        summary = "Update status",
        description = "Setting a new order status by order id"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Can't find an order with id")
    })
    @PatchMapping("/{orderId}/set-status")
    @PreAuthorize(value = "hasAnyRole('admin', 'manager')")
    public Mono<OrderResponseDto> setStatus(@PathVariable String orderId,
                                            @RequestParam(value = "status") String status) {
        return orderManagementService.updateStatus(orderId, status)
            .map(orderMapper::toDto);
    }

    @Operation(
        summary = "Submit order",
        description = "Submitting an order by manager that was newly created by customer"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Submitted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Can't find an order with id"),
        @ApiResponse(responseCode = "409", description = "Order isn't in an appropriate status")
    })
    @PutMapping("/{orderId}/submit")
    @PreAuthorize(value = "hasRole('manager')")
    public Mono<OrderResponseDto> submit(@PathVariable String orderId,
                                         @AuthenticationPrincipal
                                         JwtAuthenticationToken authenticationToken) {
        return orderManagementService.submitNewOrder(orderId, authenticationToken)
            .map(orderMapper::toDto);
    }

    @Operation(
        summary = "Accept car",
        description = "When customer delivered a car, manager accepts receiving it by a repair shop"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accepted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Can't find an order with id"),
        @ApiResponse(responseCode = "409", description = "Order isn't in an appropriate status")
    })
    @PutMapping("/{orderId}/accept-car-receiving")
    @PreAuthorize(value = "hasRole('manager')")
    public Mono<OrderResponseDto> acceptCar(@PathVariable String orderId,
                                         @AuthenticationPrincipal
                                         JwtAuthenticationToken authenticationToken) {
        return orderManagementService.acceptReceivingCarByService(orderId, authenticationToken)
            .map(orderMapper::toDto);
    }

    @Operation(
        summary = "Start working on order",
        description = "When a mechanic is ready, he accepts a car on a repair post "
            + "and starts working on it"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Started successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Can't find an order with id"),
        @ApiResponse(responseCode = "409", description = "Order isn't in an appropriate status")
    })
    @PutMapping("/{orderId}/start-work")
    @PreAuthorize(value = "hasRole('mechanic')")
    public Mono<OrderResponseDto> startWork(@PathVariable String orderId,
                                            @AuthenticationPrincipal
                                            JwtAuthenticationToken authenticationToken) {
        return orderManagementService.startWorkOnOrder(orderId, authenticationToken)
            .map(orderMapper::toDto);
    }

    @Operation(
        summary = "Complete working on order",
        description = "The mechanic confirms the completion of the work"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Can't find an order with id"),
        @ApiResponse(responseCode = "409", description = "Order isn't in an appropriate status")
    })
    @PutMapping("/{orderId}/complete-work")
    @PreAuthorize(value = "hasRole('mechanic')")
    public Mono<OrderResponseDto> completeWork(@PathVariable String orderId,
                                            @AuthenticationPrincipal
                                            JwtAuthenticationToken authenticationToken) {
        return orderManagementService.completeWork(orderId, authenticationToken)
            .map(orderMapper::toDto);
    }


    @Operation(
            summary = "Send log message",
            description = "Allows to customer, manager, or mechanic "
                    + "to send log messages to the order by id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sent successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Can't find an order with id")
    })
    @PatchMapping("{orderId}/send-message")
    @PreAuthorize(value = "hasAnyRole('mechanic', 'manager', 'customer')")
    public Mono<Void> sendMessage(@PathVariable String orderId,
                                  @RequestBody LogMessage request,
                                  @AuthenticationPrincipal JwtAuthenticationToken token) {
        setLogMessageRole(token, request);

        messageProducer.send(orderId, request);
        return Mono.empty();
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

    private void setLogMessageRole(JwtAuthenticationToken token, LogMessage logMessage) {
        logMessage.setFrom(getRole(token));
    }

    private String getRole(JwtAuthenticationToken token) {
        return token.getAuthorities().stream()
                .findFirst()
                .orElse(new SimpleGrantedAuthority("no role"))
                .getAuthority().substring(5);
    }
}
