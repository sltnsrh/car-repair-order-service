package com.salatin.orderservice.model;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Getter
@Setter
@ToString
public class Order {
    @Id
    private String id;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime carReceivedAt;
    private LocalDateTime startedWorksAt;
    private LocalDateTime finishedWorksAt;
    private LocalDateTime orderPaidAt;
    private LocalDateTime totalExecutionTime;
    private List<Part> parts = new ArrayList<>();
    private List<Work> works  = new ArrayList<>();
    private List<LogMessage> logs = new ArrayList<>();
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
