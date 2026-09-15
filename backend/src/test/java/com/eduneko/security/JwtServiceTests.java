package com.eduneko.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest 
public class JwtServiceTests {
    
    @Autowired 
    private JwtService jwtService;

    @Test 
    void debeGenerarTokenValido() {

        String token = jwtService.generarToken(1L, "daniela@correo.com", "ESTUDIANTE");

        String correo = jwtService.obtenerCorreo(token);

        assertThat(correo)
                .isEqualTo("daniela@correo.com");
    }

    @Test 
    void debeObtenerUsuarioIdDelToken() {

        String token = jwtService.generarToken(25L, "daniela@correo.com", "ESTUDIANTE");

        Long usuarioId = jwtService.obtenerUsuarioId(token);

        assertThat(usuarioId)
                .isEqualTo(25L);
    }

    @Test 
    void debeObtenerRolDelToken() {

        String token = jwtService.generarToken(1L, "daniela@correo.com", "ESTUDIANTE");

        String rol = jwtService.obtenerRol(token);

        assertThat(rol)
                .isEqualTo("ESTUDIANTE");
    }

    @Test 
    void rechazarTokenAlterado() {

        String token = jwtService.generarToken(1L, "daniela@correo.com", "ESTUDIANTE");

        String tokenAlterado = token + "abc";

        assertThat(jwtService.tokenValido(tokenAlterado))
                .isFalse();
    }
}
