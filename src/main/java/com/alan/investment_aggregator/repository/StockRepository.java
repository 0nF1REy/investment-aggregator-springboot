package com.alan.investment_aggregator.repository;

import com.alan.investment_aggregator.entity.Account;
import com.alan.investment_aggregator.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

}
