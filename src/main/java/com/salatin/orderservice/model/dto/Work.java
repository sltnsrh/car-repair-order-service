package com.salatin.orderservice.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Work {
    private Long id;
    private String name;
    private Double spentHours;
    private BigDecimal pricePerHour;
    private BigDecimal total;
    private String description;
}
