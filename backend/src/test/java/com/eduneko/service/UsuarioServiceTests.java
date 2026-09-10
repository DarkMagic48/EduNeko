package com.eduneko.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest 
public class UsuarioServiceTests {
    
    @Autowired
    private UsuarioService usuarioService;

    @Test
    void debeNormalizarCorreo() {

        String correo = "  Alumno@Correo.COM  ";

        String resultado = usuarioService.normalizarCorreo(correo);

        assertThat(resultado)
                .isEqualTo("alumno@correo.com");
    }

    @Test
    void debeMantenerCorreoNormalizado() {

        String correo = "alumno@correo.com";

        String resultado = usuarioService.normalizarCorreo(correo);

        assertThat(resultado)
                .isEqualTo("alumno@correo.com");
    }
}
