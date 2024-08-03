package com.codingtest.stock.controller.dto;

import com.codingtest.stock.enums.StockStatusCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockInitReqtDto {
    private String productId;
    private int quantity;
    private int safetyQuantity;
}
