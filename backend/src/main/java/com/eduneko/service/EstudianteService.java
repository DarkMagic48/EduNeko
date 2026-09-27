package com.eduneko.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduneko.dto.ActualizarPerfilEstudianteRequest;
import com.eduneko.entity.Estudiante;
import com.eduneko.entity.Usuario;
import com.eduneko.repository.EstudianteRepository;
import com.eduneko.repository.UsuarioRepository;

@Service 
public class EstudianteService {
    
    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;

    public EstudianteService(
            UsuarioRepository usuarioRepository,
            EstudianteRepository estudianteRepository) {

        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
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
}
