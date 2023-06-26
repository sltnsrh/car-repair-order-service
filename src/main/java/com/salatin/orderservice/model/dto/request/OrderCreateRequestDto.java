package com.salatin.orderservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class OrderCreateRequestDto {
    @NotBlank(message = "Complaints field can't be empty")
    @Size(min = 20, message = "Min complaints size should be 20")
    private String complaints;
    @NotBlank(message = "Car id field can't be empty")
    private String carId;
    private String customerId;
}
