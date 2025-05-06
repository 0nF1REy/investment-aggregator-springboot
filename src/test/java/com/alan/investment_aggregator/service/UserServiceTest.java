package com.alan.investment_aggregator.service;

import com.alan.investment_aggregator.controller.CreateUserDto;
import com.alan.investment_aggregator.controller.UpdateUserDto;
import com.alan.investment_aggregator.entity.User;
import com.alan.investment_aggregator.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Nested
    class CreateUser {

        @Test
        @DisplayName("Should create a user with success")
        void shouldCreateAUser() {

            // Arrange
            var input = new CreateUserDto(
                    "username",
                    "email@gmail.com",
                    "7343232"
            );

            doAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setUserId(UUID.randomUUID());
                return user;
            }).when(userRepository).save(userArgumentCaptor.capture());

            // Act
            var outputId = userService.createUser(input);

            // Assert
            assertNotNull(outputId);

            var userCaptured = userArgumentCaptor.getValue();
            assertEquals(input.username(), userCaptured.getUsername());
            assertEquals(input.email(), userCaptured.getEmail());
            assertEquals(input.password(), userCaptured.getPassword());
        }

        @Test
        @DisplayName("Should throw Exception when error occurs")
        void shouldThrowExceptionWhenErrorOccurs() {

            // Arrange
            doThrow(new RuntimeException()).when(userRepository).save(any());

            var input = new CreateUserDto(
                    "username",
                    "email@gmail.com",
                    "7343232"
            );

            // Act & Assert
            assertThrows(RuntimeException.class, () -> userService.createUser(input));
        }
    }

    @Nested
    class GetUserById {

        @Test
        @DisplayName("Should get user by id with success when optional is present")
        void shouldGetUserByIdWithSuccessWhenOptionalIsPresent() {

            // Arrange
            var userId = UUID.randomUUID();
            var user = new User(
                    "username",
                    "email@gmail.com",
                    "password"
            );
            user.setUserId(userId);

            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());

            // Act
            var output = userService.getUserById(userId.toString());

            // Assert
            assertTrue(output.isPresent());
            assertEquals(userId, uuidArgumentCaptor.getValue());
        }

        @Test
        @DisplayName("Should get user by id with success when optional is empty")
        void shouldGetUserByIdWithSuccessWhenOptionalIsEmpty() {

            // Arrange
            var userId = UUID.randomUUID();
            doReturn(Optional.empty()).when(userRepository).findById(uuidArgumentCaptor.capture());

            // Act
            var output = userService.getUserById(userId.toString());

            // Assert
            assertTrue(output.isEmpty());
            assertEquals(userId, uuidArgumentCaptor.getValue());
        }
    }

    @Nested
    class ListUsers {

        @Test
        @DisplayName("Should return all users with success")
        void shouldReturnAllUsersWithSuccess() {

            // Arrange
            var user = new User(
                    "username",
                    "email@gmail.com",
                    "password"
            );

            var userList = List.of(user); // ← Correção aqui
            doReturn(userList)
                    .when(userRepository)
                    .findAll();

            // Act
            var output = userService.listUsers();

            // Assert
            assertNotNull(output);
            assertEquals(userList.size(), output.size());
        }
    }

    @Nested
    class DeleteById {

        @Test
        @DisplayName("Should delete user with success when user exists")
        void shouldDeleteUserWithSuccessWhenUserExists() {

            // Arrange
            doReturn(true)
                    .when(userRepository)
                            .existsById(uuidArgumentCaptor.capture());

            doNothing()
                    .when(userRepository)
                    .deleteById(uuidArgumentCaptor.capture());

            var userId = UUID.randomUUID();

            // Act
            userService.deleteById(userId.toString());

            // Assert
            var idList = uuidArgumentCaptor.getAllValues();
            assertEquals(userId, idList.get(0));
            assertEquals(userId, idList.get(1));

            verify(userRepository, times(1)).existsById(idList.get(0));
            verify(userRepository, times(1)).existsById(idList.get(1));
        }

        @Test
        @DisplayName("Should not delete user when user NOT exists")
        void shouldNotDeleteUserWhenUserNotExists() {

            // Arrange
            doReturn(false)
                    .when(userRepository)
                    .existsById(uuidArgumentCaptor.capture());

            var userId = UUID.randomUUID();

            // Act
            userService.deleteById(userId.toString());

            // Assert
            assertEquals(userId, uuidArgumentCaptor.getValue());

            verify(userRepository, times(1))
                    .existsById(uuidArgumentCaptor.getValue());

            verify(userRepository, times(0))
                    .deleteById(any());
        }
    }

    @Nested
    class UpdateUserById {

        @Test
        @DisplayName("Should update user by id when user exists and username and password is filled")
        void shouldUpdateUserByIdWhenUserExistsAndUsernameAndPasswordIsFilled() {

            // Arrange
            var updateUserDto = new UpdateUserDto(
                    "newUsername",
                    "newPassword"
            );

            var userId = UUID.randomUUID();
            var user = new User(
                    "username",
                    "email@gmail.com",
                    "password"
            );

            user.setUserId(userId);

            doReturn(Optional.of(user))
                    .when(userRepository)
                    .findById(uuidArgumentCaptor.capture());

            doReturn(user)
                    .when(userRepository)
                    .save(userArgumentCaptor.capture());

            // Act
            userService.updateUserById(user.getUserId().toString(), updateUserDto);

            // Assert
            assertEquals(user.getUserId(), uuidArgumentCaptor.getValue());

            var userCaptured = userArgumentCaptor.getValue();

            assertEquals(updateUserDto.username(), userCaptured.getUsername());
            assertEquals(updateUserDto.password(), userCaptured.getPassword());

            verify(userRepository, times(1))
                    .findById(uuidArgumentCaptor.getValue());

            verify(userRepository, times(1))
                    .save(user);
        }

        @Test
        @DisplayName("Should not update user when user not exists")
        void shouldNotUpdateUserWhenUserNotExists() {

            // Arrange
            var updateUserDto = new UpdateUserDto(
                    "newUsername",
                    "newPassword"
            );

            var userId = UUID.randomUUID();

            doReturn(Optional.empty())
                    .when(userRepository)
                    .findById(uuidArgumentCaptor.capture());

            // Act
            userService.updateUserById(userId.toString(), updateUserDto);

            // Assert
            assertEquals(userId, uuidArgumentCaptor.getValue());

            verify(userRepository, times(1))
                    .findById(uuidArgumentCaptor.getValue());

            verify(userRepository, times(0))
                    .save(any());
        }
    }
}
