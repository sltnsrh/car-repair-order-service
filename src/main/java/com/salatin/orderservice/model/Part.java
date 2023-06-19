package com.salatin.orderservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Part {
    private Long id;
    private String name;
    private String brand;
    private Integer oemNumber;
}
