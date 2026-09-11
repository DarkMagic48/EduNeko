package com.eduneko.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.eduneko.dto.RegistroUsuarioRequest;
import com.eduneko.entity.Usuario;

@SpringBootTest
@Transactional
class LoginUsuarioServiceTests {
    
    @Autowired
    private UsuarioService usuarioService;

    @Test
    void debeAutenticarConCredencialesCorrectas() {

        RegistroUsuarioRequest registro = new RegistroUsuarioRequest();
        registro.setNombre("Ana Lopez");
        registro.setCorreo("ana@correo.com");
        registro.setPassword("MiClave123!");
        registro.setConfirmarPassword("MiClave123!");

        usuarioService.registrarUsuario(registro);

        Usuario usuario = usuarioService.autenticar(
                "ana@correo.com",
                "MiClave123!");

        assertThat(usuario).isNotNull();
        assertThat(usuario.getCorreo())
                .isEqualTo("ana@correo.com");
    }

    @Test
    void debeAutenticarCorreoConMayusculasYEspacios() {

        RegistroUsuarioRequest registro = new RegistroUsuarioRequest();
        registro.setNombre("Ana Lopez");
        registro.setCorreo("ana@correo.com");
        registro.setPassword("MiClave123!");
        registro.setConfirmarPassword("MiClave123!");

        usuarioService.registrarUsuario(registro);

        Usuario usuario = usuarioService.autenticar(
                "  ANA@CORREO.COM  ",
                "MiClave123!");

        assertThat(usuario).isNotNull();
        assertThat(usuario.getCorreo())
                .isEqualTo("ana@correo.com");
    }

    @Test
    void debeRechazarPasswordIncorrecto() {

        RegistroUsuarioRequest registro = new RegistroUsuarioRequest();
        registro.setNombre("Ana Lopez");
        registro.setCorreo("ana@correo.com");
        registro.setPassword("MiClave123!");
        registro.setConfirmarPassword("MiClave123!");

        usuarioService.registrarUsuario(registro);

        assertThatThrownBy(() ->
                usuarioService.autenticar(
                        "ana@correo.com",
                        "ClaveIncorrecta"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }

    @Test
    void debeRechazarCorreoInexistente() {

        assertThatThrownBy(() ->
                usuarioService.autenticar(
                        "noexiste@correo.com",
                        "MiClave123!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }

    @Test
    void debeRechazarUsuarioInactivo() {

        RegistroUsuarioRequest registro = new RegistroUsuarioRequest();
        registro.setNombre("Ana Lopez");
        registro.setCorreo("ana@correo.com");
        registro.setPassword("MiClave123!");
        registro.setConfirmarPassword("MiClave123!");

        Usuario usuario = usuarioService.registrarUsuario(registro);

        usuario.setActivo(false);

        assertThatThrownBy(() ->
                usuarioService.autenticar(
                        "ana@correo.com",
                        "MiClave123!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }
}
