package com.eventhub.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Configuration Tests")
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should allow login endpoint without authentication")
    void testLoginEndpoint_PermitAll() throws Exception {

        mockMvc.perform(
                post("/auth/login")
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should allow register endpoint without authentication")
    void testRegisterEndpoint_PermitAll() throws Exception {

        mockMvc.perform(
                post("/auth/register")
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should block protected endpoint without authentication")
    void testProtectedEndpoint_Unauthorized() throws Exception {

        mockMvc.perform(
                get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow swagger endpoints without authentication")
    void testSwaggerEndpoint_PermitAll() throws Exception {

        mockMvc.perform(
                get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}