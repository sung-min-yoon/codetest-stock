package com.codingtest.stock;

import com.codingtest.stock.container.TestContainerSupport;
import com.codingtest.stock.controller.dto.StockInitReqtDto;
import com.codingtest.stock.controller.dto.StockUpdateReqDto;
import com.codingtest.stock.enums.StockProcessStatusCodeEnum;
import com.codingtest.stock.enums.StockStatusCodeEnum;
import com.codingtest.stock.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest
class StockApplicationTests extends TestContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private StockService stockService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void setInitialStock_success() throws Exception {
        String json = objectMapper.writeValueAsString(StockInitReqtDto.builder()
                .productId("banana")
                .quantity(100)
                .safetyQuantity(5)
                .build());

        mockMvc.perform(post("/api/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("banana"))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.stockStatusCode").value(StockStatusCodeEnum.SALE.name()))
                .andExpect(jsonPath("$.stockProcessStatusCode").value(StockProcessStatusCodeEnum.SUCCESS.name()))
                .andDo(print());
    }

    @Test
    void setInitialStock_failure() throws Exception {
        String json = objectMapper.writeValueAsString(StockInitReqtDto.builder()
                .productId(null)
                .quantity(100)
                .safetyQuantity(5)
                .build());

        mockMvc.perform(post("/api/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockProcessStatusCode").value("FAILED"))
                .andExpect(jsonPath("$.failMessage").value("재고 초기화에 실패했습니다."))
                .andDo(print());
    }

    @Test
    void decreaseStock_success() throws Exception {
        //재고 초기화
        String initJson = objectMapper.writeValueAsString(StockInitReqtDto.builder()
                .productId("banana")
                .quantity(100)
                .safetyQuantity(5)
                .build());

        mockMvc.perform(post("/api/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(initJson));

        //재고 차감
        String decreaseJson = objectMapper.writeValueAsString(StockUpdateReqDto.builder()
                .productId("banana")
                .orderId("orderId-banana-decrease-1")
                .quantity(1)
                .build());

        mockMvc.perform(put("/api/stock/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decreaseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("banana"))
                .andExpect(jsonPath("$.orderId").value("orderId-banana-decrease-1"))
                .andExpect(jsonPath("$.quantity").value(99))
                .andExpect(jsonPath("$.stockStatusCode").value(StockStatusCodeEnum.SALE.name()))
                .andExpect(jsonPath("$.stockProcessStatusCode").value(StockProcessStatusCodeEnum.SUCCESS.name()))
                .andDo(print());
    }

    @Test
    void decreaseStock_failure() throws Exception {
        //재고 초기화
        String initJson = objectMapper.writeValueAsString(StockInitReqtDto.builder()
                .productId("banana")
                .quantity(5)
                .safetyQuantity(5)
                .build());

        mockMvc.perform(post("/api/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(initJson));

        //재고 차감
        String decreaseJson = objectMapper.writeValueAsString(StockUpdateReqDto.builder()
                .productId("banana")
                .orderId("orderId-banana-decrease-2")
                .quantity(1)
                .build());

        mockMvc.perform(put("/api/stock/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decreaseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("banana"))
                .andExpect(jsonPath("$.orderId").value("orderId-banana-decrease-2"))
                .andExpect(jsonPath("$.stockProcessStatusCode").value(StockProcessStatusCodeEnum.FAILED.name()))
                .andExpect(jsonPath("$.failMessage").value("품절되었습니다."))
                .andDo(print());

        //초기 재고 유지 확인
        mockMvc.perform(get("/api/stock/banana")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("banana"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.stockStatusCode").value(StockStatusCodeEnum.SOLD_OUT.name()))
                .andExpect(jsonPath("$.stockProcessStatusCode").value(StockProcessStatusCodeEnum.SUCCESS.name()))
                .andDo(print());
    }

    @Test
    void increaseStock_success() throws Exception {
        //재고 초기화
        String initJson = objectMapper.writeValueAsString(StockInitReqtDto.builder()
                .productId("banana")
                .quantity(100)
                .safetyQuantity(5)
                .build());

        mockMvc.perform(post("/api/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(initJson));

        //재고 증가
        String decreaseJson = objectMapper.writeValueAsString(StockUpdateReqDto.builder()
                .productId("banana")
                .orderId("orderId-banana-increase-1")
                .quantity(1)
                .build());

        mockMvc.perform(put("/api/stock/increase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decreaseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("banana"))
                .andExpect(jsonPath("$.orderId").value("orderId-banana-increase-1"))
                .andExpect(jsonPath("$.quantity").value(101))
                .andExpect(jsonPath("$.stockStatusCode").value(StockStatusCodeEnum.SALE.name()))
                .andExpect(jsonPath("$.stockProcessStatusCode").value(StockProcessStatusCodeEnum.SUCCESS.name()))
                .andDo(print());
    }

    @Test
    void increaseStock_failure() throws Exception {
        //재고 초기화
        String initJson = objectMapper.writeValueAsString(StockInitReqtDto.builder()
                .productId("banana")
                .quantity(100)
                .safetyQuantity(5)
                .build());

        mockMvc.perform(post("/api/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(initJson));

        //재고 증가
        String decreaseJson = objectMapper.writeValueAsString(StockUpdateReqDto.builder()
                .productId(null)
                .orderId("orderId-banana-increase-1")
                .quantity(1)
                .build());

        mockMvc.perform(put("/api/stock/increase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decreaseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("orderId-banana-increase-1"))
                .andExpect(jsonPath("$.stockProcessStatusCode").value(StockProcessStatusCodeEnum.FAILED.name()))
                .andExpect(jsonPath("$.failMessage").value("재고 증/차감 처리에 실패했습니다."))
                .andDo(print());

        //초기 재고 유지 확인
        mockMvc.perform(get("/api/stock/banana")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("banana"))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.stockStatusCode").value(StockStatusCodeEnum.SALE.name()))
                .andExpect(jsonPath("$.stockProcessStatusCode").value(StockProcessStatusCodeEnum.SUCCESS.name()))
                .andDo(print());
    }

    @Test
    void getStock_success() throws Exception {
        //재고 초기화
        String initJson = objectMapper.writeValueAsString(StockInitReqtDto.builder()
                .productId("banana")
                .quantity(200)
                .safetyQuantity(5)
                .build());

        mockMvc.perform(post("/api/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(initJson));

        mockMvc.perform(get("/api/stock/banana")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("banana"))
                .andExpect(jsonPath("$.quantity").value(200))
                .andExpect(jsonPath("$.stockStatusCode").value(StockStatusCodeEnum.SALE.name()))
                .andExpect(jsonPath("$.stockProcessStatusCode").value(StockProcessStatusCodeEnum.SUCCESS.name()))
                .andDo(print());
    }

    @Test
    void getStock_failure() throws Exception {
        mockMvc.perform(get("/api/stock/없는상품")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("없는상품"))
                .andExpect(jsonPath("$.stockProcessStatusCode").value(StockProcessStatusCodeEnum.NOT_FOUND.name()))
                .andExpect(jsonPath("$.failMessage").value("상품이 존재 하지않습니다."))
                .andDo(print());
    }
}
