package com.codingtest.stock.service.dto;

import com.codingtest.stock.enums.StockStatusCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRedisDto {
    private String productId;
    private int quantity;
    private int safetyQuantity;
    private StockStatusCodeEnum stockStatusCode;
    private int version;
}
