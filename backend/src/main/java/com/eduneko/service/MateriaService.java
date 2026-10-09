package com.eduneko.service;

import com.eduneko.entity.Estudiante;
import com.eduneko.entity.Materia;
import com.eduneko.entity.Usuario;
import com.eduneko.dto.SeleccionarMateriasRequest;
import com.eduneko.repository.EstudianteRepository;
import com.eduneko.repository.MateriaRepository;
import com.eduneko.repository.UsuarioRepository;
import com.eduneko.repository.EstudianteMateriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstudianteMateriaRepository estudianteMateriaRepository;

    public MateriaService(
            MateriaRepository materiaRepository,
            EstudianteRepository estudianteRepository,
            UsuarioRepository usuarioRepository,
            EstudianteMateriaRepository estudianteMateriaRepository) {

        this.materiaRepository = materiaRepository;
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.estudianteMateriaRepository = estudianteMateriaRepository;
    }

    public List<Materia> obtenerMateriasDisponibles(String correo) {

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

        if (estudiante.getGradoEscolar() == null) {
            throw new IllegalStateException(
                    "El estudiante debe completar su perfil académico");
        }

        return materiaRepository.findActivasByGrado(
                estudiante.getGradoEscolar());
    }

    @Transactional
public List<Materia> seleccionarMaterias(
        String correo,
        SeleccionarMateriasRequest request) {

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

    if (estudiante.getGradoEscolar() == null) {
        throw new IllegalStateException(
                "El estudiante debe completar su perfil académico");
    }

    List<Materia> materiasDisponibles =
            materiaRepository.findActivasByGrado(
                    estudiante.getGradoEscolar());

    Set<Long> idsPermitidos = materiasDisponibles.stream()
            .map(Materia::getId)
            .collect(Collectors.toSet());

    Set<Long> idsSeleccionados =
            Set.copyOf(request.getMateriaIds());

    if (!idsPermitidos.containsAll(idsSeleccionados)) {
        throw new IllegalArgumentException(
                "Una o más materias no corresponden al grado del estudiante");
    }

    estudianteMateriaRepository.eliminarPorEstudiante(
            estudiante.getId());

    for (Long materiaId : idsSeleccionados) {
        estudianteMateriaRepository.guardarSeleccion(
                estudiante.getId(),
                materiaId);
    }

    return estudianteMateriaRepository
            .findMateriasByEstudiante(estudiante.getId());
  }
}