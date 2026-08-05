package com.eventhub.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eventhub.domain.dto.CreateUserRequest;
import com.eventhub.domain.dto.UserDto;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.security.JwtAuthenticationFilter;
import com.eventhub.security.JwtProvider;
import com.eventhub.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private JwtProvider jwtProvider;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        private UUID userId;

        private UserDto userDto;

        private CreateUserRequest createUserRequest;

        @BeforeEach
        void setUp() {

                userId = UUID.randomUUID();

                userDto = new UserDto(
                                userId,
                                "test@example.com",
                                "John",
                                "Doe",
                                null,
                                null);

                createUserRequest = new CreateUserRequest(
                                "test@example.com",
                                "password123",
                                "John",
                                "Doe");
        }

        @Test
        void testCreateUser() throws Exception {

                when(userService.createUser(any(CreateUserRequest.class)))
                                .thenReturn(userDto);

                mockMvc.perform(
                                post("/users")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(createUserRequest)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message")
                                                .value("User created successfully"))
                                .andExpect(jsonPath("$.data.email")
                                                .value("test@example.com"));

                verify(userService)
                                .createUser(any(CreateUserRequest.class));
        }

        @Test
        void testGetUserById() throws Exception {

                when(userService.getUserById(userId))
                                .thenReturn(userDto);

                mockMvc.perform(
                                get("/users/" + userId))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.email")
                                                .value("test@example.com"));

                verify(userService)
                                .getUserById(userId);
        }

        @Test
        void testGetAllUsers() throws Exception {

                when(userService.getAllUsers())
                                .thenReturn(List.of(userDto));

                mockMvc.perform(
                                get("/users"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].email")
                                                .value("test@example.com"));

                verify(userService)
                                .getAllUsers();
        }

        @Test
        void testGetUserByEmail() throws Exception {

                when(userService.getUserByEmail("test@example.com"))
                                .thenReturn(userDto);

                mockMvc.perform(
                                get("/users/email/test@example.com"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.email")
                                                .value("test@example.com"));

                verify(userService)
                                .getUserByEmail("test@example.com");
        }

        @Test
        void testUpdateUser() throws Exception {

                when(userService.updateUser(
                                any(UUID.class),
                                any(UserDto.class)))
                                .thenReturn(userDto);

                mockMvc.perform(
                                put("/users/" + userId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(userDto)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.email")
                                                .value("test@example.com"));

                verify(userService)
                                .updateUser(any(UUID.class), any(UserDto.class));
        }

        @Test
        void testDeleteUser() throws Exception {

                doNothing()
                                .when(userService)
                                .deleteUser(userId);

                mockMvc.perform(
                                delete("/users/" + userId))
                                .andDo(print())
                                .andExpect(status().isNoContent());

                verify(userService)
                                .deleteUser(userId);
        }

        @Test
        void testCreateUser_InvalidRequest() throws Exception {

                CreateUserRequest invalidRequest = new CreateUserRequest(
                                "",
                                "",
                                "",
                                "");

                mockMvc.perform(
                                post("/users")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testGetUserById_NotFound() throws Exception {

                when(userService.getUserById(userId))
                                .thenThrow(new ResourceNotFoundException(
                                                "User not found"));

                mockMvc.perform(
                                get("/users/" + userId))
                                .andExpect(status().isNotFound());
        }

}