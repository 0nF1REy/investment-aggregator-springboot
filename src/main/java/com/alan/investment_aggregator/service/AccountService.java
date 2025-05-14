package com.alan.investment_aggregator.service;

import com.alan.investment_aggregator.controller.dto.AccountStockResponseDto;
import com.alan.investment_aggregator.controller.dto.AssociateAccountStockDto;
import com.alan.investment_aggregator.entity.AccountStock;
import com.alan.investment_aggregator.entity.AccountStockId;
import com.alan.investment_aggregator.repository.AccountRepository;
import com.alan.investment_aggregator.repository.AccountStockRepository;
import com.alan.investment_aggregator.repository.StockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private AccountRepository accountRepository;

    private StockRepository stockRepository;

    private AccountStockRepository accountStockRepository;

    public AccountService(AccountRepository accountRepository,
                          StockRepository stockRepository,
                          AccountStockRepository accountStockRepository) {
        this.accountRepository = accountRepository;
        this.stockRepository = stockRepository;
        this.accountStockRepository = accountStockRepository;
    }

    public void associateStock(String accountId, AssociateAccountStockDto dto) {
        try {
            var account = accountRepository.findById(UUID.fromString(accountId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));

            var stock = stockRepository.findById(dto.stockId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ação não encontrada"));

            var id = new AccountStockId(account.getAccountId(), stock.getStockId());
            var entity = new AccountStock(
                    id,
                    account,
                    stock,
                    dto.quantity()
            );

            accountStockRepository.save(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao associar ação à conta", e);
        }
    }

    public List<AccountStockResponseDto> listStocks(String accountId) {

        var account = accountRepository.findById(UUID.fromString(accountId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));

        return account.getAccountStocks()
                .stream()
                .map(as -> new AccountStockResponseDto(as.getStock().getStockId(), as.getQuantity(), 0.0))
                .toList();
    }
}
