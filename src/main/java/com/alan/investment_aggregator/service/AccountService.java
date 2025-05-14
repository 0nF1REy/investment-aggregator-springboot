package com.alan.investment_aggregator.service;

import com.alan.investment_aggregator.client.BrapiClient;
import com.alan.investment_aggregator.controller.dto.AccountStockResponseDto;
import com.alan.investment_aggregator.controller.dto.AssociateAccountStockDto;
import com.alan.investment_aggregator.entity.AccountStock;
import com.alan.investment_aggregator.entity.AccountStockId;
import com.alan.investment_aggregator.repository.AccountRepository;
import com.alan.investment_aggregator.repository.AccountStockRepository;
import com.alan.investment_aggregator.repository.StockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final AccountStockRepository accountStockRepository;
    private final BrapiClient brapiClient;
    private final Dotenv dotenv;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          StockRepository stockRepository,
                          AccountStockRepository accountStockRepository,
                          BrapiClient brapiClient,
                          Dotenv dotenv) {
        this.accountRepository = accountRepository;
        this.stockRepository = stockRepository;
        this.accountStockRepository = accountStockRepository;
        this.brapiClient = brapiClient;
        this.dotenv = dotenv;
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
                .map(as ->
                        new AccountStockResponseDto(
                        as.getStock().getStockId(),
                        as.getQuantity(),
                                getTotal(as.getQuantity(), as.getStock().getStockId())
                ))
                .toList();
    }

    private double getTotal(Integer quantity, String stockId) {
        String token = dotenv.get("TOKEN");

        var response = brapiClient.getQuote(token, stockId);

        if (response.results() == null || response.results().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ação não encontrada na Brapi");
        }

        var stockDto = response.results().getFirst();

        if (stockDto.regularMarketPrice() == 0.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preço de mercado não disponível para a ação " + stockId);
        }

        BigDecimal total = BigDecimal.valueOf(quantity * stockDto.regularMarketPrice());

        total = total.setScale(2, RoundingMode.HALF_UP);

        return total.doubleValue();
    }
}
