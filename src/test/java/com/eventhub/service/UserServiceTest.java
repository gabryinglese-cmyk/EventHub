package com.eventhub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eventhub.domain.dto.CreateUserRequest;
import com.eventhub.domain.dto.UserDto;
import com.eventhub.domain.entity.User;
import com.eventhub.domain.mapper.UserMapper;
import com.eventhub.exception.DuplicateResourceException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private UserMapper userMapper;

        @InjectMocks
        private UserService userService;

        private UUID userId;
        private User user;
        private CreateUserRequest createUserRequest;
        private UserDto userDto;

        @BeforeEach
        void setUp() {

                userId = UUID.randomUUID();

                user = new User();
                user.setId(userId);
                user.setEmail("test@example.com");
                user.setPassword("hashedPassword");
                user.setFirstName("John");
                user.setLastName("Doe");

                createUserRequest = new CreateUserRequest(
                                "test@example.com",
                                "password123",
                                "John",
                                "Doe");

                userDto = new UserDto(
                                userId,
                                "test@example.com",
                                "John",
                                "Doe",
                                null,
                                null);
        }

        @Test
        @DisplayName("Should create user successfully")
        void testCreateUser_Success() {

                when(userRepository.existsByEmail(createUserRequest.email()))
                                .thenReturn(false);

                when(passwordEncoder.encode(createUserRequest.password()))
                                .thenReturn("hashedPassword");

                when(userRepository.save(any(User.class)))
                                .thenReturn(user);

                when(userMapper.toDto(user))
                                .thenReturn(userDto);

                UserDto result = userService.createUser(createUserRequest);

                assertThat(result).isNotNull();
                assertThat(result.email())
                                .isEqualTo("test@example.com");

                verify(userRepository)
                                .existsByEmail(createUserRequest.email());

                verify(passwordEncoder)
                                .encode(createUserRequest.password());

                verify(userRepository)
                                .save(any(User.class));

                verify(userMapper)
                                .toDto(user);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void testCreateUser_DuplicateEmail() {

                when(userRepository.existsByEmail(createUserRequest.email()))
                                .thenReturn(true);

                assertThatThrownBy(() -> userService.createUser(createUserRequest))
                                .isInstanceOf(DuplicateResourceException.class)
                                .hasMessageContaining("User with email already exists");

                verify(userRepository, never())
                                .save(any(User.class));
        }

        @Test
        @DisplayName("Should retrieve user by id")
        void testGetUserById_Success() {

                when(userRepository.findById(userId))
                                .thenReturn(Optional.of(user));

                when(userMapper.toDto(user))
                                .thenReturn(userDto);

                UserDto result = userService.getUserById(userId);

                assertThat(result)
                                .isNotNull();

                assertThat(result.id())
                                .isEqualTo(userId);

                verify(userRepository)
                                .findById(userId);

                verify(userMapper)
                                .toDto(user);
        }

        @Test
        @DisplayName("Should throw exception when user not found by id")
        void testGetUserById_NotFound() {

                when(userRepository.findById(userId))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.getUserById(userId))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(userMapper, never())
                                .toDto(any());
        }

        @Test
        @DisplayName("Should retrieve user by email")
        void testGetUserByEmail_Success() {

                when(userRepository.findByEmail(user.getEmail()))
                                .thenReturn(Optional.of(user));

                when(userMapper.toDto(user))
                                .thenReturn(userDto);

                UserDto result = userService.getUserByEmail(user.getEmail());

                assertThat(result)
                                .isNotNull();

                assertThat(result.email())
                                .isEqualTo("test@example.com");

                verify(userRepository)
                                .findByEmail(user.getEmail());

                verify(userMapper)
                                .toDto(user);
        }

        @Test
        @DisplayName("Should throw exception when email does not exist")
        void testGetUserByEmail_NotFound() {

                when(userRepository.findByEmail(user.getEmail()))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.getUserByEmail(user.getEmail()))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(userMapper, never())
                                .toDto(any());
        }

        @Test
        @DisplayName("Should retrieve all users")
        void testGetAllUsers_Success() {

                when(userRepository.findAll())
                                .thenReturn(List.of(user));

                when(userMapper.toDto(user))
                                .thenReturn(userDto);

                List<UserDto> result = userService.getAllUsers();

                assertThat(result)
                                .hasSize(1);

                assertThat(result.get(0).email())
                                .isEqualTo("test@example.com");

                verify(userRepository)
                                .findAll();

                verify(userMapper)
                                .toDto(user);
        }

        @Test
        @DisplayName("Should update user successfully with same email")
        void testUpdateUser_SameEmail() {

                when(userRepository.findById(userId))
                                .thenReturn(Optional.of(user));

                when(userRepository.save(user))
                                .thenReturn(user);

                when(userMapper.toDto(user))
                                .thenReturn(userDto);

                UserDto result = userService.updateUser(userId, userDto);

                assertThat(result)
                                .isNotNull();

                verify(userRepository)
                                .save(user);

                verify(userRepository, never())
                                .existsByEmail(any());
        }

        @Test
        @DisplayName("Should update user successfully with new email")
        void testUpdateUser_NewEmailAvailable() {

                UserDto updatedDto = new UserDto(
                                userId,
                                "new@example.com",
                                "John",
                                "Doe",
                                null,
                                null);

                when(userRepository.findById(userId))
                                .thenReturn(Optional.of(user));

                when(userRepository.existsByEmail("new@example.com"))
                                .thenReturn(false);

                when(userRepository.save(user))
                                .thenReturn(user);

                when(userMapper.toDto(user))
                                .thenReturn(updatedDto);

                UserDto result = userService.updateUser(userId, updatedDto);

                assertThat(result.email())
                                .isEqualTo("new@example.com");

                verify(userRepository)
                                .existsByEmail("new@example.com");

                verify(userRepository)
                                .save(user);
        }

        @Test
        @DisplayName("Should throw exception when updating with existing email")
        void testUpdateUser_DuplicateEmail() {

                UserDto updatedDto = new UserDto(
                                userId,
                                "existing@example.com",
                                "John",
                                "Doe",
                                null,
                                null);

                when(userRepository.findById(userId))
                                .thenReturn(Optional.of(user));

                when(userRepository.existsByEmail("existing@example.com"))
                                .thenReturn(true);

                assertThatThrownBy(() -> userService.updateUser(userId, updatedDto))
                                .isInstanceOf(DuplicateResourceException.class);

                verify(userRepository, never())
                                .save(any());
        }

        @Test
        @DisplayName("Should delete user successfully")
        void testDeleteUser_Success() {

                when(userRepository.findById(userId))
                                .thenReturn(Optional.of(user));

                userService.deleteUser(userId);

                verify(userRepository)
                                .delete(user);
        }

        @Test
        @DisplayName("Should throw exception when deleting missing user")
        void testDeleteUser_NotFound() {

                when(userRepository.findById(userId))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.deleteUser(userId))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(userRepository, never())
                                .delete(any());
        }
}