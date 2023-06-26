package com.salatin.orderservice.service.mapper;

import com.salatin.orderservice.model.Order;
import com.salatin.orderservice.model.dto.request.OrderCreateRequestDto;
import com.salatin.orderservice.model.dto.response.OrderResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toModel(OrderCreateRequestDto requestDto);

    OrderResponseDto toDto(Order order);
}
