package com.codingtest.stock.controller.dto;

import com.codingtest.stock.enums.StockProcessStatusCodeEnum;
import com.codingtest.stock.enums.StockStatusCodeEnum;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockResDto {
    private String productId;
    private int quantity;
    private int safetyQuantity;
    private StockStatusCodeEnum stockStatusCode;
    private StockProcessStatusCodeEnum stockProcessStatusCode;
    private String failMessage;
}
