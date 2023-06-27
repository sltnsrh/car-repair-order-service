package com.salatin.orderservice.model;

import lombok.Data;

@Data
public class Car {
    private String id;
    private String brand;
    private String model;
    private String licencePlate;
    private Short productionYear;
    private String vin;
    private String ownerId;
    private String createdAt;
    private String updatedAt;
}
