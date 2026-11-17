package com.eduneko.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import com.eduneko.dto.ActualizarPerfilEstudianteRequest;
import com.eduneko.dto.ActualizarPreferenciasRequest;
import com.eduneko.entity.PreferenciaAprendizaje;
import com.eduneko.entity.Estudiante;
import com.eduneko.entity.Usuario;
import com.eduneko.repository.EstudianteRepository;
import com.eduneko.repository.UsuarioRepository;
import com.eduneko.repository.PreferenciaAprendizajeRepository;
import com.eduneko.repository.DiagnosticoIntentoRepository;
import com.eduneko.entity.DiagnosticoIntento;


@Service 
public class EstudianteService {
    
    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PreferenciaAprendizajeRepository preferenciaAprendizajeRepository;
    private final DiagnosticoIntentoRepository diagnosticoIntentoRepository;

    public EstudianteService(
            UsuarioRepository usuarioRepository,
            EstudianteRepository estudianteRepository,
            PreferenciaAprendizajeRepository preferenciaAprendizajeRepository,
            DiagnosticoIntentoRepository diagnosticoIntentoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.preferenciaAprendizajeRepository = preferenciaAprendizajeRepository;
        this.diagnosticoIntentoRepository = diagnosticoIntentoRepository;
    }

    /** Consultar el perfil del estudiante autenticado sin modificarlo. */
    @Transactional(readOnly = true)
    public Estudiante obtenerPerfilActual(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return estudianteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado"));
    }

    /** Consultar las preferencias actuales para precargar el formulario. */
    @Transactional(readOnly = true)
    public PreferenciaAprendizaje obtenerPreferenciasActuales(String correo) {
        Estudiante estudiante = obtenerPerfilActual(correo);
        return preferenciaAprendizajeRepository.findByEstudianteId(estudiante.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aún no se han configurado las preferencias"));
    }

    @Transactional
    public Estudiante actualizarPerfil(
            String correo,
            ActualizarPerfilEstudianteRequest request) {

        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"));

        Estudiante estudiante = estudianteRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Perfil de estudiante no encontrado"));

        // Si cambia el grado, cerrar de forma no destructiva los intentos
        // pendientes del grado anterior antes de guardar el nuevo perfil.
        // Esto permite iniciar otro diagnóstico sin violar el índice único
        // de intentos EN_PROGRESO (estudiante, materia).
        if (estudiante.getGradoEscolar() != null &&
                !estudiante.getGradoEscolar().equals(request.getGradoEscolar())) {
            List<DiagnosticoIntento> pendientes = diagnosticoIntentoRepository
                    .findByEstudianteIdAndEstado(estudiante.getId(), "EN_PROGRESO");
            for (DiagnosticoIntento intento : pendientes) {
                intento.setEstado("CANCELADO");
                intento.setFechaFin(LocalDateTime.now());
            }
            diagnosticoIntentoRepository.saveAll(pendientes);
        }

        estudiante.setEdad(request.getEdad());
        estudiante.setGradoEscolar(request.getGradoEscolar());

        return estudianteRepository.save(estudiante);
    }

    @Transactional
public PreferenciaAprendizaje actualizarPreferencias(
        String correo,
        ActualizarPreferenciasRequest request) {

    Usuario usuario = usuarioRepository
            .findByCorreo(correo)
            .orElseThrow(() ->
                    new IllegalArgumentException("Usuario no encontrado"));

    Estudiante estudiante = estudianteRepository
            .findByUsuarioId(usuario.getId())
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Perfil de estudiante no encontrado"));

    PreferenciaAprendizaje preferencias =
            preferenciaAprendizajeRepository
                    .findByEstudianteId(estudiante.getId())
                    .orElseGet(() -> {
                        PreferenciaAprendizaje nueva =
                                new PreferenciaAprendizaje();
                        nueva.setEstudiante(estudiante);
                        return nueva;
                    });

    preferencias.setNivelExplicacion(
            request.getNivelExplicacion());

    preferencias.setNivelRetoPreferido(
            request.getNivelRetoPreferido());

    preferencias.setRitmoEstudio(
            request.getRitmoEstudio());

    preferencias.setPrefiereLecturaEscritura(
            request.isPrefiereLecturaEscritura());

    preferencias.setPrefiereVisual(
            request.isPrefiereVisual());

    preferencias.setPrefiereAuditivo(
            request.isPrefiereAuditivo());

    preferencias.setPrefierePractica(
            request.isPrefierePractica());

    return preferenciaAprendizajeRepository.save(preferencias);
        }

        
    /**
     * Consulta si el estudiante autenticado ya completó
     * su configuración inicial (onboarding).
     *
     * Este método únicamente lee el estado almacenado
     * en PostgreSQL; no modifica información.
     *
     * @param correo Correo del usuario autenticado.
     * @return true si completó el onboarding; false si está pendiente.
     */
    @Transactional(readOnly = true)
    public boolean obtenerEstadoOnboarding(String correo) {

        // Buscar al usuario autenticado mediante su correo.
        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"));

        // Recuperar el perfil de estudiante asociado.
        Estudiante estudiante = estudianteRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Perfil de estudiante no encontrado"));

        // Devolver el valor real almacenado en PostgreSQL.
        return estudiante.isOnboardingCompletado();
    }

}
