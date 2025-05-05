package com.alan.investment_aggregator.service;

import com.alan.investment_aggregator.controller.CreateUserDto;
import com.alan.investment_aggregator.controller.UpdateUserDto;
import com.alan.investment_aggregator.entity.User;
import com.alan.investment_aggregator.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UUID createUser(CreateUserDto createUserDto) {
        try {

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
}