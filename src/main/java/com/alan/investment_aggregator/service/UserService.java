package com.alan.investment_aggregator.service;

import com.alan.investment_aggregator.controller.dto.AccountResponseDto;
import com.alan.investment_aggregator.controller.dto.CreateAccountDto;
import com.alan.investment_aggregator.controller.dto.CreateUserDto;
import com.alan.investment_aggregator.controller.dto.UpdateUserDto;
import com.alan.investment_aggregator.entity.Account;
import com.alan.investment_aggregator.entity.BillingAddress;
import com.alan.investment_aggregator.entity.User;
import com.alan.investment_aggregator.repository.AccountRepository;
import com.alan.investment_aggregator.repository.BillingAddressRepository;
import com.alan.investment_aggregator.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BillingAddressRepository billingAddressRepository;

    public UserService(UserRepository userRepository,
                       AccountRepository accountRepository,
                       BillingAddressRepository billingAddressRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.billingAddressRepository = billingAddressRepository;
    }

    @Transactional
    public UUID createUser(CreateUserDto createUserDto) {
        try {
            if (userRepository.existsByEmail(createUserDto.email())) {
                throw new IllegalArgumentException("O e-mail já está em uso.");
            }

            if (createUserDto.username() == null || createUserDto.username().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be blank");
            }
            if (createUserDto.email() == null || createUserDto.email().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be blank");
            }
            if (createUserDto.password() == null || createUserDto.password().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be blank");
            }

            if (!createUserDto.email().contains("@")) {
                throw new IllegalArgumentException("Invalid email format");
            }

            // DTO -> ENTITY
            var entity = new User(
                    createUserDto.username(),
                    createUserDto.email(),
                    createUserDto.password()
            );

            var userSaved = userRepository.save(entity);
            return userSaved.getUserId();

        } catch (Exception e) {

            System.err.println("Erro ao criar usuário: " + e.getMessage());
            e.printStackTrace();

            throw new RuntimeException("Falha ao criar usuário", e);
        }
    }

    public Optional<User> getUserById(String userId) {
        try {
            return userRepository.findById(UUID.fromString(userId));
        } catch (IllegalArgumentException e) {

            System.err.println("ID de usuário inválido: " + userId);
            return Optional.empty();
        }
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public void updateUserById(String userId,
                               UpdateUserDto updateUserDto) {
        var id = UUID.fromString(userId);

        var userEntity = userRepository.findById(id);

        if (userEntity.isPresent()) {
            var user = userEntity.get();

            if (updateUserDto.username() != null) {
                user.setUsername(updateUserDto.username());
            }

            if (updateUserDto.password() != null) {
                user.setPassword(updateUserDto.password());
            }

            userRepository.save(user);
        }
    }

    public void deleteById(String userId) {
        var id = UUID.fromString(userId);

        var userExists = userRepository.existsById(id);

        if (userExists) {
            userRepository.deleteById(id);
        }
    }

    public void createAccount(String userId, CreateAccountDto createAccountDto) {

        var user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var account = new Account();
        account.setUser(user);
        account.setDescription(createAccountDto.description());

        var billingAddress = new BillingAddress();
        billingAddress.setStreet(createAccountDto.street());
        billingAddress.setNumber(createAccountDto.number());
        billingAddress.setAccount(account);

        account.setBillingAddress(billingAddress);

        accountRepository.save(account);
    }

    public List<AccountResponseDto> listAccounts(String userId) {
        var user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return user.getAccounts()
                .stream()
                .map(ac ->
                        new AccountResponseDto(ac.getAccountId().toString(), ac.getDescription()))
                .toList();
    }
}