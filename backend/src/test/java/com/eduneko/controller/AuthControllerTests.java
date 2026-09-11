package com.eduneko.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional

class AuthControllerTests {
    
    @Autowired
    private MockMvc mockMvc;

    @Test
    void debeRegistrarUsuarioDesdeEndpoint() throws Exception {

        String json = """
                {
                  "nombre": "Ana Lopez",
                  "correo": "  Ana@Correo.COM  ",
                  "password": "MiClave123!",
                  "confirmarPassword": "MiClave123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("Ana Lopez"))
                .andExpect(jsonPath("$.correo").value("ana@correo.com"))
                .andExpect(jsonPath("$.mensaje")
                        .value("Usuario registrado correctamente"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void debeRechazarCorreoInvalido() throws Exception {

    String json = """
            {
              "nombre": "Ana Lopez",
              "correo": "correo-invalido",
              "password": "MiClave123!",
              "confirmarPassword": "MiClave123!"
            }
            """;

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensaje")
                    .value("Datos de registro inválidos"))
            .andExpect(jsonPath("$.errores.correo")
                    .value("El correo no tiene un formato válido"));
    }

    @Test
    void debeRechazarPasswordCorto() throws Exception {

        String json = """
                {
                "nombre": "Ana Lopez",
                "correo": "ana@correo.com",
                "password": "123",
                "confirmarPassword": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.password")
                        .value("La contraseña debe tener al menos 8 caracteres"));
    }

    @Test
    void debeRechazarPasswordsDiferentes() throws Exception {

        String json = """
                {
                "nombre": "Ana Lopez",
                "correo": "ana@correo.com",
                "password": "MiClave123!",
                "confirmarPassword": "OtraClave123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("Las contraseñas no coinciden"));
    }

    @Test
    void debeRechazarCorreoDuplicado() throws Exception {

        String json = """
                {
                "nombre": "Ana Lopez",
                "correo": "ana@correo.com",
                "password": "MiClave123!",
                "confirmarPassword": "MiClave123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("El correo ya está registrado"));
    }
}
