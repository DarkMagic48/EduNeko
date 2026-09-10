package com.eduneko.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.eduneko.entity.Estudiante;
import com.eduneko.entity.Usuario;

@SpringBootTest
@Transactional  
class UsuarioRepositoryTests {
    
    @Autowired 
    private UsuarioRepository usuarioRepository;

    @Autowired 
    private EstudianteRepository estudianteRepository;

    @Test 
    void debeGuardarYConsultarUsuarioConEstudiante() {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Prueba");
        usuario.setCorreo("prueba@eduneko.test");
        usuario.setPasswordHash("hash-de-prueba");

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuarioGuardado);
        estudiante.setEdad((short) 13);
        estudiante.setGradoEscolar((short) 2);

        estudianteRepository.save(estudiante);

        Optional<Usuario> usuarioEncontrado =
                usuarioRepository.findByCorreo("prueba@eduneko.test");

        Optional<Estudiante> estudianteEncontrado =
                estudianteRepository.findByUsuarioId(usuarioGuardado.getId());

        assertThat(usuarioEncontrado).isPresent();
        assertThat(estudianteEncontrado).isPresent();

        assertThat(usuarioEncontrado.get().getNombre())
                .isEqualTo("Usuario Prueba");

        assertThat(estudianteEncontrado.get().getEdad())
                .isEqualTo((short) 13);

        assertThat(estudianteEncontrado.get().getGradoEscolar())
                .isEqualTo((short) 2);
    }
}
