package com.codingtest.stock.service;

import com.codingtest.stock.controller.dto.StockInitReqtDto;
import com.codingtest.stock.controller.dto.StockInitRestDto;
import com.codingtest.stock.controller.dto.StockReqDto;
import com.codingtest.stock.controller.dto.StockResDto;
import com.codingtest.stock.controller.dto.StockUpdateReqDto;
import com.codingtest.stock.controller.dto.StockUpdateResDto;
import com.codingtest.stock.enums.StockOperationCodeEnum;
import com.codingtest.stock.enums.StockProcessStatusCodeEnum;
import com.codingtest.stock.enums.StockStatusCodeEnum;
import com.codingtest.stock.exception.StockException;
import com.codingtest.stock.jpa.entity.StockHistoryEntity;
import com.codingtest.stock.jpa.entity.StockMainEntity;
import com.codingtest.stock.jpa.repository.StockHistoryRepository;
import com.codingtest.stock.jpa.repository.StockMainRepository;
import com.codingtest.stock.service.dto.StockRedisDto;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RJsonBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private static final String STOCK_CACHE_PREFIX = "stock_";
    public static final String LOCK = "_lock";
    private static final int LOCK_WAIT_TIME = 5; // seconds
    private static final int LOCK_LEASE_TIME = 1; // second
    private static final int LOCK_RETRY_COUNT = 3;
    private static final Duration BUCKET_TTL = Duration.ofMinutes(5);

    private final StockMainRepository mainRepository;
    private final StockHistoryRepository historyRepository;
    private final RedissonClient redissonClient;

    @Transactional
    public StockInitRestDto setInitialStock(StockInitReqtDto stockInitReqtDto) {
        String productId = stockInitReqtDto.getProductId();
        RLock lock = redissonClient.getLock(STOCK_CACHE_PREFIX + productId + LOCK);
        getLock(lock);
        try {
            int version = 1;
            int quantity = stockInitReqtDto.getQuantity();
            int safetyQuantity = stockInitReqtDto.getSafetyQuantity();

            Optional<StockMainEntity> optionalStockMainEntity = mainRepository.findById(productId);
            if (optionalStockMainEntity.isPresent()) {
                version = optionalStockMainEntity.get().getVersion() + 1;
            }

            //Redis Bucket 생성
            RJsonBucket<StockRedisDto> bucket = redissonClient.getJsonBucket(STOCK_CACHE_PREFIX + productId, new JacksonCodec<>(StockRedisDto.class));
            StockStatusCodeEnum stockStatusCode = updateStock(StockOperationCodeEnum.INITIALISE, quantity, safetyQuantity, productId, version, bucket);

            return StockInitRestDto.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .stockStatusCode(stockStatusCode)
                    .stockProcessStatusCode(StockProcessStatusCodeEnum.SUCCESS)
                    .build();

        } catch (StockException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new StockException(StockProcessStatusCodeEnum.FAILED, "재고 초기화에 실패했습니다.", e);
        } finally {
            lock.unlock();
        }

    }

    @Transactional
    public StockUpdateResDto decreaseStock(StockUpdateReqDto stockUpdateReqDto) {
        String productId = stockUpdateReqDto.getProductId();
        RLock lock = redissonClient.getLock(STOCK_CACHE_PREFIX + productId + LOCK);
        getLock(lock);

        try {
            RJsonBucket<StockRedisDto> bucket = getStockBucket(productId);
            StockRedisDto stockRedisDto = bucket.get();

            int quantityChange = stockUpdateReqDto.getQuantity();
            validateDecreaseStock(stockRedisDto, quantityChange);

            int currentStockQuantity = stockRedisDto.getQuantity();
            int safetyQuantity = stockRedisDto.getSafetyQuantity();

            String orderId = stockUpdateReqDto.getOrderId();
            int version = stockRedisDto.getVersion();
            int calculateResultQuantity = currentStockQuantity - quantityChange;
            StockStatusCodeEnum stockStatusCode = updateStock(StockOperationCodeEnum.DECREASE, orderId, calculateResultQuantity, safetyQuantity, productId, version, bucket);

            return createStockUpdateResDto(productId, stockUpdateReqDto.getOrderId(), calculateResultQuantity, stockStatusCode);

        } catch (StockException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new StockException(StockProcessStatusCodeEnum.FAILED, "재고 증/차감 처리에 실패 했습니다.", e);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public StockUpdateResDto increaseStock(StockUpdateReqDto stockUpdateReqDto) {
        String productId = stockUpdateReqDto.getProductId();
        RLock lock = redissonClient.getLock(STOCK_CACHE_PREFIX + productId + LOCK);
        getLock(lock);

        try {
            String orderId = stockUpdateReqDto.getOrderId();
            int quantityChange = stockUpdateReqDto.getQuantity();

            RJsonBucket<StockRedisDto> bucket = getStockBucket(productId);
            StockRedisDto stockRedisDto = bucket.get();
            int currentStockQuantity = stockRedisDto.getQuantity();
            int safetyQuantity = stockRedisDto.getSafetyQuantity();
            int version = stockRedisDto.getVersion();

            int calculateResultQuantity = currentStockQuantity + quantityChange;
            StockStatusCodeEnum stockStatusCode = updateStock(StockOperationCodeEnum.INCREASE, orderId, calculateResultQuantity, safetyQuantity, productId, version, bucket);

            return createStockUpdateResDto(productId, stockUpdateReqDto.getOrderId(), calculateResultQuantity, stockStatusCode);

        } catch (StockException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new StockException(StockProcessStatusCodeEnum.FAILED, "재고 증/차감 처리에 실패했습니다.", e);
        } finally {
            lock.unlock();
        }
    }


    @Transactional(readOnly = true)
    public StockResDto getStock(StockReqDto stockReqDto) {
        try {
            StockRedisDto stockRedisDto = getStockBucket(stockReqDto.getProductId()).get();

            return StockResDto.builder()
                    .productId(stockReqDto.getProductId())
                    .quantity(stockRedisDto.getQuantity())
                    .safetyQuantity(stockRedisDto.getSafetyQuantity())
                    .stockStatusCode(stockRedisDto.getStockStatusCode())
                    .stockProcessStatusCode(StockProcessStatusCodeEnum.SUCCESS)
                    .build();

        } catch (StockException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new StockException(StockProcessStatusCodeEnum.FAILED, "재고 조회에 실패했습니다.", e);
        }
    }

    private StockStatusCodeEnum updateStock(StockOperationCodeEnum stockOperationCode, int quantity, int safetyQuantity, String productId, int version, RJsonBucket<StockRedisDto> bucket) {
        return this.updateStock(stockOperationCode, null, quantity, safetyQuantity, productId, version, bucket);
    }

    private StockStatusCodeEnum updateStock(StockOperationCodeEnum stockOperationCode, String orderId, int quantity, int safetyQuantity, String productId, int version, RJsonBucket<StockRedisDto> bucket) {
        StockStatusCodeEnum stockStatusCode = quantity - safetyQuantity > 0 ? StockStatusCodeEnum.SALE : StockStatusCodeEnum.SOLD_OUT;
        //재고 메인 DB 갱신
        StockMainEntity stockMainEntity = StockMainEntity.builder()
                .productId(productId)
                .quantity(quantity)
                .safetyQuantity(safetyQuantity)
                .stockStatusCode(stockStatusCode)
                .version(version)
                .build();
        mainRepository.save(stockMainEntity);

        //재고 히스토리 DB 저장
        historyRepository.save(StockHistoryEntity.builder()
                .stockMainEntity(stockMainEntity)
                .orderId(orderId)
                .quantity(quantity)
                .safetyQuantity(safetyQuantity)
                .stockStatusCode(stockStatusCode)
                .version(version)
                .timestamp(LocalDateTime.now())
                .stockOperationCode(stockOperationCode)
                .build());

        bucket.set(new StockRedisDto(productId, quantity, safetyQuantity, stockStatusCode, version), BUCKET_TTL);

        return stockStatusCode;
    }

    private void getLock(RLock lock) {
        try {
            for (int i = 0; i < LOCK_RETRY_COUNT; i++) {
                if (lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS)) {
                    return;
                }
            }
        } catch (InterruptedException e) {
            throw new StockException(StockProcessStatusCodeEnum.FAILED, "재고 처리 Lock 획득에 실패했습니다.", e);
        }
    }

    private RJsonBucket<StockRedisDto> getStockBucket(String productId) {
        RJsonBucket<StockRedisDto> bucket = redissonClient.getJsonBucket(STOCK_CACHE_PREFIX + productId, new JacksonCodec<>(StockRedisDto.class));
        StockRedisDto stockRedisDto = bucket.get();
        if (stockRedisDto != null) {
            return bucket;
        }

        Optional<StockMainEntity> optionalStockMainEntity = mainRepository.findById(productId);
        if (optionalStockMainEntity.isEmpty()) {
            throw new StockException(StockProcessStatusCodeEnum.NOT_FOUND, "상품이 존재 하지않습니다.");
        }

        StockMainEntity stockMainEntity = optionalStockMainEntity.get();
        bucket.set(new StockRedisDto(stockMainEntity.getProductId(), stockMainEntity.getQuantity(), stockMainEntity.getSafetyQuantity(), stockMainEntity.getStockStatusCode(), stockMainEntity.getVersion()), BUCKET_TTL);

        return bucket;
    }

    private void validateDecreaseStock(StockRedisDto stockRedisDto, int quantityChange) {
        if (StockStatusCodeEnum.SOLD_OUT == stockRedisDto.getStockStatusCode()) {
            throw new StockException(StockProcessStatusCodeEnum.FAILED, "품절되었습니다.");
        }

        int availablePurchaseQuantity = stockRedisDto.getQuantity() - stockRedisDto.getSafetyQuantity(); // 구매 가능 수량
        if (availablePurchaseQuantity < quantityChange) { //남은 재고 수량이 구매 수량보다 적을 경우
            throw new StockException(StockProcessStatusCodeEnum.FAILED, "재고가 부족합니다.");
        }
    }

    private StockUpdateResDto createStockUpdateResDto(String productId, String orderId, int quantity, StockStatusCodeEnum stockStatusCode) {
        return StockUpdateResDto.builder()
                .productId(productId)
                .orderId(orderId)
                .quantity(quantity)
                .stockStatusCode(stockStatusCode)
                .stockProcessStatusCode(StockProcessStatusCodeEnum.SUCCESS)
                .build();
    }
}
