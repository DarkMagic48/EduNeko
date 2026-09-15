package com.eduneko.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.eduneko.security.JwtService;

import com.eduneko.dto.RegistroUsuarioRequest;
import com.eduneko.entity.Usuario;
import com.eduneko.service.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional

class AuthControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    @Autowired 
    private JwtService jwtService;
    @Autowired 
    private UsuarioService usuarioService;

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
                    .value("Datos inválidos"))
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

        @Test
    void debeIniciarSesionCorrectamente() throws Exception {

        String registro = """
                {
                "nombre": "Daniela",
                "correo": "daniela@correo.com",
                "password": "MiClave123!",
                "confirmarPassword": "MiClave123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro))
                .andExpect(status().isCreated());

        String login = """
                {
                "correo": "daniela@correo.com",
                "password": "MiClave123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Daniela"))
                .andExpect(jsonPath("$.correo").value("daniela@correo.com"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.mensaje")
                        .value("Inicio de sesión correcto"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void debeIniciarSesionConCorreoEnMayusculasYEspacios() throws Exception {

        String registro = """
                {
                "nombre": "Daniela",
                "correo": "daniela@correo.com",
                "password": "MiClave123!",
                "confirmarPassword": "MiClave123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro))
                .andExpect(status().isCreated());

        String login = """
                {
                "correo": "  DANIELA@CORREO.COM  ",
                "password": "MiClave123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("daniela@correo.com"));
    }

    @Test
    void debeRechazarPasswordIncorrectoEnLogin() throws Exception {

        String registro = """
                {
                "nombre": "Daniela",
                "correo": "daniela@correo.com",
                "password": "MiClave123!",
                "confirmarPassword": "MiClave123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro))
                .andExpect(status().isCreated());

        String login = """
                {
                "correo": "daniela@correo.com",
                "password": "Incorrecta123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("Correo o contraseña incorrectos"));
    }

    @Test
    void debeRechazarCorreoInexistenteEnLogin() throws Exception {

        String login = """
                {
                "correo": "noexiste@correo.com",
                "password": "MiClave123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("Correo o contraseña incorrectos"));
    }

    @Test
    void debeRechazarCorreoInvalidoEnLogin() throws Exception {

        String login = """
                {
                "correo": "correo-invalido",
                "password": "MiClave123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("Datos inválidos"))
                .andExpect(jsonPath("$.errores.correo")
                        .value("El correo no tiene un formato válido"));
    }

    @Test 
    void debeRechazarEndpointProtegidoSinToken() throws Exception {

        mockMvc.perform(get("/api/usuario/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test 
    void deberPermitirEndpointProtegidoConTokenValido() throws Exception {

        RegistroUsuarioRequest registro =
            new RegistroUsuarioRequest();

    registro.setNombre("Daniela");
    registro.setCorreo("daniela@correo.com");
    registro.setPassword("MiClave123!");
    registro.setConfirmarPassword("MiClave123!");

    Usuario usuario =
            usuarioService.registrarUsuario(registro);

    String token = jwtService.generarToken(
            usuario.getId(),
            usuario.getCorreo(),
            usuario.getRol());

    mockMvc.perform(get("/api/usuario/me")
            .header(
                    "Authorization",
                    "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correo")
                    .value("daniela@correo.com"));
    }

    @Test 
    void debeRechazarEndpointProtegidoConTokenAlterado() throws Exception {

        String token = jwtService.generarToken(1L, "daniela@correo.com", "ESTUDIANTE");

        String tokenAlterado = token + "abc";

        mockMvc.perform(get("/api/usuario/me")
                .header("Authorization", "Bearer " + tokenAlterado))
                .andExpect(status().isUnauthorized());       
    }

    @Test
    void debeRechazarTokenDeUsuarioInactivo() throws Exception {

    RegistroUsuarioRequest registro =
            new RegistroUsuarioRequest();

    registro.setNombre("Daniela");
    registro.setCorreo("daniela@correo.com");
    registro.setPassword("MiClave123!");
    registro.setConfirmarPassword("MiClave123!");

    Usuario usuario =
            usuarioService.registrarUsuario(registro);

    String token = jwtService.generarToken(
            usuario.getId(),
            usuario.getCorreo(),
            usuario.getRol());

    usuario.setActivo(false);

    mockMvc.perform(get("/api/usuario/me")
            .header(
                    "Authorization",
                    "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }
}