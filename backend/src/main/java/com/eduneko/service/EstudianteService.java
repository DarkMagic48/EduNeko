package com.eduneko.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduneko.dto.ActualizarPerfilEstudianteRequest;
import com.eduneko.dto.ActualizarPreferenciasRequest;
import com.eduneko.entity.PreferenciaAprendizaje;
import com.eduneko.entity.Estudiante;
import com.eduneko.entity.Usuario;
import com.eduneko.repository.EstudianteRepository;
import com.eduneko.repository.UsuarioRepository;
import com.eduneko.repository.PreferenciaAprendizajeRepository;


@Service 
public class EstudianteService {
    
    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PreferenciaAprendizajeRepository preferenciaAprendizajeRepository;

    public EstudianteService(
            UsuarioRepository usuarioRepository,
            EstudianteRepository estudianteRepository, 
            PreferenciaAprendizajeRepository preferenciaAprendizajeRepository) {

        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.preferenciaAprendizajeRepository = preferenciaAprendizajeRepository;
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
}
