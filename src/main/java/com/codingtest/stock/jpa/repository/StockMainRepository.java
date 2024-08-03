package com.codingtest.stock.jpa.repository;

import com.codingtest.stock.jpa.entity.StockMainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockMainRepository extends JpaRepository<StockMainEntity, String> {
}
