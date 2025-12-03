package com.campus.marketplace.integration;

import com.campus.marketplace.dto.LoginRequest;
import com.campus.marketplace.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerLoginAndFetchProfile() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Integration Tester");
        registerRequest.setEmail(randomEmail());
        registerRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(registerRequest.getEmail()))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(registerRequest.getEmail());
        loginRequest.setPassword(registerRequest.getPassword());

        String loginToken = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(registerRequest.getEmail()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginToken).get("token").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(registerRequest.getEmail()))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }
}
