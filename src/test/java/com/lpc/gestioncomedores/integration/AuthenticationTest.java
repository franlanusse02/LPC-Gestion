package com.lpc.gestioncomedores.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpc.gestioncomedores.dtos.auth.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  // Use application-test.properties
public class AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterLoginAndAuth() throws Exception {
        // 1. Register a new user
        String registerJson = """
            {
                  "cuil": 20304050607,
                  "rol": "ADMIN",
                  "password": "miPassword123"
            }
        """;

        mockMvc.perform(post("/api/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                        .andExpect(status().isCreated());

        // 2. Login with the registered user
        String loginJson = """
            {
                  "cuil": 20304050607,
                  "password": "miPassword123"
            }
        """;

        String responseBody = mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        // Deserialize JSON to DTO
        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
        String token = loginResponse.token();

        // 3. Test an authenticated endpoint
        mockMvc.perform(get("/api/usuarios/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}