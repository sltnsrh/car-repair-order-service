package com.salatin.orderservice.orderservice.model;

import com.salatin.userservice.model.status.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private Long id;
    //    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime customerRequestedAt;
    private LocalDateTime startRepairAt;
    private LocalDateTime finishRepairAt;
    private LocalDateTime submittedAt;
    private List<Part> parts;
    private List<Work> works;
    private LocalDateTime totalExecutionTime;
    private BigDecimal total;
    private OrderStatus status;
}
