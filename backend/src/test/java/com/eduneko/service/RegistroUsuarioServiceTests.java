package com.eduneko.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.eduneko.dto.RegistroUsuarioRequest;
import com.eduneko.entity.Estudiante;
import com.eduneko.entity.Usuario;
import com.eduneko.repository.EstudianteRepository;
import com.eduneko.repository.UsuarioRepository;

@SpringBootTest 
@Transactional 
class RegistroUsuarioServiceTests {
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Test
    void debeRegistrarUsuarioConEstudiante() {

        RegistroUsuarioRequest request = new RegistroUsuarioRequest();
        request.setNombre("Ana Lopez");
        request.setCorreo("  Ana@Correo.COM  ");
        request.setPassword("MiClave123!");
        request.setConfirmarPassword("MiClave123!");

        Usuario usuario = usuarioService.registrarUsuario(request);

        Optional<Usuario> usuarioEncontrado =
                usuarioRepository.findByCorreo("ana@correo.com");

        Optional<Estudiante> estudianteEncontrado =
                estudianteRepository.findByUsuarioId(usuario.getId());

        assertThat(usuarioEncontrado).isPresent();
        assertThat(estudianteEncontrado).isPresent();

        assertThat(usuarioEncontrado.get().getCorreo())
                .isEqualTo("ana@correo.com");

        assertThat(usuarioEncontrado.get().getPasswordHash())
                .isNotEqualTo("MiClave123!");

        assertThat(
                usuarioService.passwordCoincide(
                        "MiClave123!",
                        usuarioEncontrado.get().getPasswordHash()))
                .isTrue();
    }

    @Test
    void noDebeRegistrarCorreoDuplicado() {

        RegistroUsuarioRequest request = new RegistroUsuarioRequest();
        request.setNombre("Ana Lopez");
        request.setCorreo("ana@correo.com");
        request.setPassword("MiClave123!");
        request.setConfirmarPassword("MiClave123!");

        usuarioService.registrarUsuario(request);

        RegistroUsuarioRequest duplicado = new RegistroUsuarioRequest();
        duplicado.setNombre("Otro Usuario");
        duplicado.setCorreo(" ANA@CORREO.COM ");
        duplicado.setPassword("OtraClave123!");
        duplicado.setConfirmarPassword("OtraClave123!");

        assertThatThrownBy(() ->
                usuarioService.registrarUsuario(duplicado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El correo ya está registrado");
    }

    @Test
    void noDebeRegistrarSiPasswordsNoCoinciden() {

        RegistroUsuarioRequest request = new RegistroUsuarioRequest();
        request.setNombre("Ana Lopez");
        request.setCorreo("ana@correo.com");
        request.setPassword("MiClave123!");
        request.setConfirmarPassword("OtraClave123!");

        assertThatThrownBy(() ->
                usuarioService.registrarUsuario(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Las contraseñas no coinciden");
    }

}
