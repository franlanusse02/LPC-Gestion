package com.lpc.gestioncomedores.integration;

import com.lpc.gestioncomedores.dtos.auth.AuthResponse;
import com.lpc.gestioncomedores.dtos.auth.RegisterRequest;
import com.lpc.gestioncomedores.models.enums.UsuarioRol;
import com.lpc.gestioncomedores.repositories.UsuarioRepository;
import com.lpc.gestioncomedores.security.JwtTokenProvider;
import com.lpc.gestioncomedores.services.UsuarioService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Needed for @BeforeAll non-static
public class ComedorCrudTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UsuarioRepository userRepository;

    private String token;

    @BeforeAll
    void setupUserAndToken() {
        // Clean DB in case previous tests left data
        userRepository.deleteAll();

        // Create user directly via service
        RegisterRequest req = new RegisterRequest(Long.valueOf("20447881315"), UsuarioRol.ADMIN, "password123");
        AuthResponse res = userService.registrar(req);

        // Generate JWT token directly
        token = res.token();
    }

    @Test
    void testCrearComedor() throws Exception {
        String content = """
                {
                  "nombre": "Galicia"
                }
                """;
        mockMvc.perform(post("/api/comedor")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content))
                    .andExpect(status().isCreated());
    }
}
