package com.eventhub.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventhub.constant.AppConstants;
import com.eventhub.domain.dto.CreateUserRequest;
import com.eventhub.domain.dto.UserDto;
import com.eventhub.domain.entity.User;
import com.eventhub.domain.mapper.UserMapper;
import com.eventhub.exception.DuplicateResourceException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    AppConstants.USER_ALREADY_EXISTS + request.email());
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        log.info("User created successfully with id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    public UserDto getUserById(UUID userId) {
        log.info("Fetching user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.USER_NOT_FOUND + userId));

        return userMapper.toDto(user);
    }

    public UserDto getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));

        return userMapper.toDto(user);
    }

    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUser(UUID userId, UserDto userDto) {
        log.info("Updating user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.USER_NOT_FOUND + userId));

        if (!user.getEmail().equals(userDto.email()) &&
                userRepository.existsByEmail(userDto.email())) {
            throw new DuplicateResourceException(
                    AppConstants.USER_ALREADY_EXISTS + userDto.email());
        }

        user.setEmail(userDto.email());
        user.setFirstName(userDto.firstName());
        user.setLastName(userDto.lastName());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", userId);

        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.USER_NOT_FOUND + userId));

        userRepository.delete(user);
        log.info("User deleted successfully with id: {}", userId);
    }
}