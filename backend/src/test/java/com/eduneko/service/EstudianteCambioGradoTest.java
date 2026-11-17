package com.eduneko.service;

import com.eduneko.dto.ActualizarPerfilEstudianteRequest;
import com.eduneko.entity.DiagnosticoIntento;
import com.eduneko.entity.Estudiante;
import com.eduneko.entity.Usuario;
import com.eduneko.repository.DiagnosticoIntentoRepository;
import com.eduneko.repository.EstudianteRepository;
import com.eduneko.repository.PreferenciaAprendizajeRepository;
import com.eduneko.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Verifica que cambiar de grado no elimine el historial académico. */
@ExtendWith(MockitoExtension.class)
class EstudianteCambioGradoTest {
    @Mock UsuarioRepository usuarios;
    @Mock EstudianteRepository estudiantes;
    @Mock PreferenciaAprendizajeRepository preferencias;
    @Mock DiagnosticoIntentoRepository intentos;
    @InjectMocks EstudianteService servicio;

    private Estudiante estudianteInicial() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        Estudiante estudiante = new Estudiante();
        estudiante.setId(8L);
        estudiante.setGradoEscolar((short) 1);
        estudiante.setEdad((short) 12);
        when(usuarios.findByCorreo("estudiante@prueba.mx"))
                .thenReturn(Optional.of(usuario));
        when(estudiantes.findByUsuarioId(3L))
                .thenReturn(Optional.of(estudiante));
        when(estudiantes.save(estudiante)).thenReturn(estudiante);
        return estudiante;
    }

    private ActualizarPerfilEstudianteRequest solicitud(short grado) {
        ActualizarPerfilEstudianteRequest datos = new ActualizarPerfilEstudianteRequest();
        datos.setEdad((short) 13);
        datos.setGradoEscolar(grado);
        return datos;
    }

    @Test
    void cancelaSoloIntentosPendientesSiCambiaGrado() {
        Estudiante estudiante = estudianteInicial();
        DiagnosticoIntento pendiente = new DiagnosticoIntento();
        pendiente.setEstado("EN_PROGRESO");
        when(intentos.findByEstudianteIdAndEstado(8L, "EN_PROGRESO"))
                .thenReturn(List.of(pendiente));

        Estudiante actualizado = servicio.actualizarPerfil(
                "estudiante@prueba.mx", solicitud((short) 2));

        assertSame(estudiante, actualizado);
        assertEquals((short) 2, actualizado.getGradoEscolar());
        assertEquals("CANCELADO", pendiente.getEstado());
        assertNotNull(pendiente.getFechaFin());
        verify(intentos).saveAll(List.of(pendiente));
        // No eliminar intentos ni resultados: el historial permanece intacto.
        verify(intentos, never()).delete(any());
    }

    @Test
    void mantenerGradoNoCancelaNingunDiagnostico() {
        Estudiante estudiante = estudianteInicial();
        servicio.actualizarPerfil("estudiante@prueba.mx", solicitud((short) 1));
        assertEquals((short) 1, estudiante.getGradoEscolar());
        verifyNoInteractions(intentos);
    }
}
