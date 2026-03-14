package com.lpc.gestioncomedores.integration;

import com.lpc.gestioncomedores.dtos.auth.LoginResponse;
import com.lpc.gestioncomedores.dtos.auth.RegisterRequest;
import com.lpc.gestioncomedores.models.Comedor;
import com.lpc.gestioncomedores.models.PuntoDeVenta;
import com.lpc.gestioncomedores.models.enums.UsuarioRol;
import com.lpc.gestioncomedores.repositories.ComedorRepository;
import com.lpc.gestioncomedores.repositories.PuntoDeVentaRepository;
import com.lpc.gestioncomedores.repositories.UsuarioRepository;
import com.lpc.gestioncomedores.security.JwtTokenProvider;
import com.lpc.gestioncomedores.services.UsuarioService;
import org.junit.jupiter.api.*;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderedCreationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private ComedorRepository comedorRepository;

    @Autowired
    private PuntoDeVentaRepository puntoDeVentaRepository;

    private String token;

    @BeforeAll
    void setupUserAndToken() {
        // Clean DB in case previous tests left data
        puntoDeVentaRepository.deleteAll();
        comedorRepository.deleteAll();
        userRepository.deleteAll();


        // Create user directly via service
        RegisterRequest req = new RegisterRequest(Long.valueOf("20447881315"), UsuarioRol.ADMIN, "password123");
        LoginResponse res = userService.registrar(req);

        // Generate JWT token directly
        token = res.token();
    }

    @Test
    @Order(1)
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

    @Test
    @Order(2)
    void testCrearPuntoVenta() throws Exception {
        Comedor comedor = comedorRepository.findAll().getFirst();
        String content = String.format("""
                {
                  "nombre": "Venta",
                  "comedorId":%d
                }
                """, comedor.getId());

        mockMvc.perform(post("/api/puntodeventa")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                        .andExpect(status().isCreated());
    }

    @Test
    @Order(3)
    void testCrearCierreCaja() throws Exception {
        PuntoDeVenta ptoVenta = puntoDeVentaRepository.findAll().getFirst();
        String content = String.format("""
            {
              "puntoVentaId": %d,
              "fechaOperacion": "2026-03-03",
              "totalPlatosVendidos": 5,
              "comentarios": "Gran dia"
            }
            """, ptoVenta.getId());
        mockMvc.perform(post("/api/cierre")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated());
    }
}
