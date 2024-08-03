package com.codingtest.stock.jpa.repository;

import com.codingtest.stock.jpa.entity.StockHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockHistoryRepository extends JpaRepository<StockHistoryEntity, Long> {
}
