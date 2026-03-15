package com.lpc.gestioncomedores.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Minimum 256-bit secret for HS256
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "test-secret-key-that-is-long-enough-for-hs256-algorithm");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L); // 1h
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtTokenProvider.generateToken(20304050607L, "Martin", "ADMIN");
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void getCuilFromToken_shouldReturnOriginalCuil() {
        Long cuil = 20304050607L;
        String token = jwtTokenProvider.generateToken(cuil, "Martin", "ADMIN");

        Long extracted = jwtTokenProvider.getCuilFromToken(token);

        assertThat(extracted).isEqualTo(cuil);
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtTokenProvider.generateToken(20304050607L, "Martin", "ADMIN");
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_withInvalidToken_shouldThrowException() {
        assertThatThrownBy(() -> jwtTokenProvider.validateToken("token.invalido.abc"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token inválido");
    }

    @Test
    void validateToken_withExpiredToken_shouldThrowException() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "jwtSecret",
                "test-secret-key-that-is-long-enough-for-hs256-algorithm");
        ReflectionTestUtils.setField(expiredProvider, "jwtExpirationMs", -1000L); // already expired

        String expiredToken = expiredProvider.generateToken(20304050607L, "Martin", "ADMIN");

        assertThatThrownBy(() -> expiredProvider.validateToken(expiredToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token expirado");
    }
}
