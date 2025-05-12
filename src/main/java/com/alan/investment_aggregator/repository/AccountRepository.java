package com.alan.investment_aggregator.repository;

import com.alan.investment_aggregator.entity.Account;
import com.alan.investment_aggregator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

}
