package com.codingtest.stock.controller;

import com.codingtest.stock.controller.dto.*;
import com.codingtest.stock.enums.StockProcessStatusCodeEnum;
import com.codingtest.stock.exception.StockException;
import com.codingtest.stock.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    public ResponseEntity<StockInitRestDto> setInitialStock(@RequestBody StockInitReqtDto stockInitReqtDto) {
        try {
            return ResponseEntity.ok(stockService.setInitialStock(stockInitReqtDto));
        } catch (Exception e) {
            return ResponseEntity.ok(StockInitRestDto.builder()
                    .productId(stockInitReqtDto.getProductId())
                    .quantity(stockInitReqtDto.getQuantity())
                    .stockProcessStatusCode(getStatusFromException(e))
                    .failMessage(getFailMessage(e))
                    .build());
        }
    }

    @PutMapping("/decrease")
    public ResponseEntity<StockUpdateResDto> decreaseStock(@RequestBody StockUpdateReqDto stockUpdateReqDto) {
        try {
            return ResponseEntity.ok(stockService.decreaseStock(stockUpdateReqDto));
        } catch (Exception e) {
            return ResponseEntity.ok(StockUpdateResDto.builder()
                    .productId(stockUpdateReqDto.getProductId())
                    .orderId(stockUpdateReqDto.getOrderId())
                    .stockProcessStatusCode(getStatusFromException(e))
                    .failMessage(getFailMessage(e))
                    .build());
        }
    }

    @PutMapping("/increase")
    public ResponseEntity<StockUpdateResDto> increaseStock(@RequestBody StockUpdateReqDto stockUpdateReqDto) {
        try {
            return ResponseEntity.ok(stockService.increaseStock(stockUpdateReqDto));
        } catch (Exception e) {
            return ResponseEntity.ok(StockUpdateResDto.builder()
                    .productId(stockUpdateReqDto.getProductId())
                    .orderId(stockUpdateReqDto.getOrderId())
                    .stockProcessStatusCode(getStatusFromException(e))
                    .failMessage(getFailMessage(e))
                    .build());
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<StockResDto> getStock(@PathVariable(name = "productId") String productId) {
        try {
            return ResponseEntity.ok(stockService.getStock(StockReqDto.builder()
                    .productId(productId)
                    .build()));
        } catch (Exception e) {
            return ResponseEntity.ok(StockResDto.builder()
                    .productId(productId)
                    .stockProcessStatusCode(getStatusFromException(e))
                    .failMessage(getFailMessage(e))
                    .build());
        }
    }

    private String getFailMessage(Exception e) {
        return e instanceof StockException ?
                e.getMessage() :
                "재고 처리 중 요류가 발생 했습니다.";
    }

    private StockProcessStatusCodeEnum getStatusFromException(Exception e) {
        return e instanceof StockException ?
                ((StockException) e).getStockProcessStatusCodeEnum() :
                StockProcessStatusCodeEnum.FAILED;
    }
}