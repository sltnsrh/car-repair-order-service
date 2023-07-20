package com.salatin.orderservice.model.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LogMessage {
    private String from;
    private String text;
    private LocalDateTime time = LocalDateTime.now();
}
