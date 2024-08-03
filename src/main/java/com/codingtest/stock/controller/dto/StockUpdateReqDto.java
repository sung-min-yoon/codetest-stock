package com.codingtest.stock.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateReqDto {
    private String productId;
    private String orderId;
    private int quantity;
}
