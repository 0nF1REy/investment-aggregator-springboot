package com.alan.investment_aggregator.repository;

import com.alan.investment_aggregator.entity.Account;
import com.alan.investment_aggregator.entity.AccountStock;
import com.alan.investment_aggregator.entity.AccountStockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountStockRepository extends JpaRepository<AccountStock, AccountStockId> {

}
