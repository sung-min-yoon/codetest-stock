package com.codingtest.stock.exception;

import com.codingtest.stock.enums.StockProcessStatusCodeEnum;

public class StockException extends RuntimeException {
    private StockProcessStatusCodeEnum stockProcessStatusCodeEnum;

    public StockProcessStatusCodeEnum getStockProcessStatusCodeEnum() {
        return stockProcessStatusCodeEnum;
    }

    public StockException(StockProcessStatusCodeEnum stockProcessStatusCodeEnum, String message, Throwable cause) {
        super(message, cause);
        this.stockProcessStatusCodeEnum = stockProcessStatusCodeEnum;
    }

    public StockException(StockProcessStatusCodeEnum stockProcessStatusCodeEnum, String message) {
        super(message);
        this.stockProcessStatusCodeEnum = stockProcessStatusCodeEnum;
    }

}
