package com.salatin.orderservice.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class Order {
    private Long id;
//    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime carReceivedAt;
    private LocalDateTime startedWorksAt;
    private LocalDateTime finishedWorksAt;
    private LocalDateTime orderPaidAt;
    private LocalDateTime totalExecutionTime;
    private List<Part> parts;
    private List<Work> works;
    private List<LogMessage> logs;
    private String complaints;
    private BigDecimal totalCost;
    private BigDecimal discount;
    private BigDecimal toPay;
    private String feedback;
    private OrderStatus status;
    private String carId;
    private String customerId;
    private String mechanicId;
    private String managerId;
}
