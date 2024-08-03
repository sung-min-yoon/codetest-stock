package com.codingtest.stock.controller.dto;

import com.codingtest.stock.enums.StockProcessStatusCodeEnum;
import com.codingtest.stock.enums.StockStatusCodeEnum;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockUpdateResDto {
    private String productId;
    private String orderId;
    private int quantity;
    private StockStatusCodeEnum stockStatusCode;
    private StockProcessStatusCodeEnum stockProcessStatusCode;
    private String failMessage;
}
