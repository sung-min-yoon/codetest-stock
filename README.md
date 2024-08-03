# 코딩테스트 과제 : 온라인 쇼핑몰에서 상품 재고 차감 API 구현

## 요약
- 메인, 히스토리 테이블로 재고등록 & 증/차감 로직만 구현함
- 테이블은 컨테이너 실행 시 자동 생성됨

## 요구사항
- Docker Desktop 설치
- JDK 17 설치

## 의존성
- Java 17
- Spring Boot 3.3.2
- Spring MVC 3.3.2
- Spring Data JPA 3.3.2
- Spring Data Redis 3.3.2
- Redisson 3.33.0
- Lombok 1.18.22
- Testcontainers 1.20.1

## 사용된 Docker Image
- mysql:latest
- redislabs/rejson:latest

## API 호출
- IntelliJ IDEA의 HTTP Client를 사용하여 API 호출
- root 경로의 `StockApplication.http` 파일 참조

## 테스트 코드 실행
- `StockApplicationTests` 클래스에서 테스트 실행
- `Testcontainers`를 사용하여 MySql, Redis 컨테이너 자동 실행 후 테스트 진행

## 자동 생성되는 테이블 스크립트
```
#재고 메인
CREATE TABLE STOCK_MAIN
(
PRODUCT_ID        VARCHAR(255)              NOT NULL
PRIMARY KEY,
QUANTITY          INT                       NULL,
SAFETY_QUANTITY   INT                       NULL,
STOCK_STATUS_CODE ENUM ('SALE', 'SOLD_OUT') NULL,
VERSION           INT                       NULL
);

#재고 이력
CREATE TABLE STOCK_HISTORY
(
ID                   BIGINT AUTO_INCREMENT
PRIMARY KEY,
ORDER_ID             VARCHAR(255)                                NULL,
QUANTITY             INT                                         NULL,
SAFETY_QUANTITY      INT                                         NULL,
STOCK_OPERATION_CODE ENUM ('DECREASE', 'INCREASE', 'INITIALISE') NULL,
STOCK_STATUS_CODE    ENUM ('SALE', 'SOLD_OUT')                   NULL,
TIMESTAMP            DATETIME(6)                                 NULL,
VERSION              INT                                         NULL,
PRODUCT_ID           VARCHAR(255)                                NULL
);

CREATE INDEX IDX_STOCK_HISTORY_ORDER_ID
ON STOCK_HISTORY (ORDER_ID);

CREATE INDEX IDX_STOCK_HISTORY_PRODUCT_ID
ON STOCK_HISTORY (PRODUCT_ID);
```
