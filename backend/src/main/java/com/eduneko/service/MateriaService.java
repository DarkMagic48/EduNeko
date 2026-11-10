
package com.eduneko.service;

import com.eduneko.dto.SeleccionarMateriasRequest;
import com.eduneko.entity.Estudiante;
import com.eduneko.entity.Materia;
import com.eduneko.entity.PreferenciaAprendizaje;
import com.eduneko.entity.Usuario;
import com.eduneko.repository.EstudianteMateriaRepository;
import com.eduneko.repository.EstudianteRepository;
import com.eduneko.repository.MateriaRepository;
import com.eduneko.repository.PreferenciaAprendizajeRepository;
import com.eduneko.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MateriaService {

    // Repositorios necesarios para consultar y guardar
    // la información académica del estudiante.
    private final MateriaRepository materiaRepository;
    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstudianteMateriaRepository estudianteMateriaRepository;
    private final PreferenciaAprendizajeRepository preferenciaAprendizajeRepository;

    public MateriaService(
            MateriaRepository materiaRepository,
            EstudianteRepository estudianteRepository,
            UsuarioRepository usuarioRepository,
            EstudianteMateriaRepository estudianteMateriaRepository,
            PreferenciaAprendizajeRepository preferenciaAprendizajeRepository) {

        this.materiaRepository = materiaRepository;
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.estudianteMateriaRepository = estudianteMateriaRepository;
        this.preferenciaAprendizajeRepository = preferenciaAprendizajeRepository;
    }

    // Obtiene las materias disponibles para el grado escolar
    // del estudiante autenticado.
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

        // El grado escolar es necesario para determinar
        // qué materias corresponden al estudiante.
        if (estudiante.getGradoEscolar() == null) {
            throw new IllegalStateException(
                    "El estudiante debe completar su perfil académico");
        }

        return materiaRepository.findActivasByGrado(
                estudiante.getGradoEscolar());
    }

    // Guarda las materias seleccionadas y verifica si el
    // estudiante ha completado su configuración inicial.
    @Transactional
    public List<Materia> seleccionarMaterias(
            String correo,
            SeleccionarMateriasRequest request) {

        // 1. Identificar al usuario autenticado.
        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"));

        // 2. Recuperar su perfil de estudiante.
        Estudiante estudiante = estudianteRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Perfil de estudiante no encontrado"));

        // 3. Comprobar que tenga un grado escolar.
        if (estudiante.getGradoEscolar() == null) {
            throw new IllegalStateException(
                    "El estudiante debe completar su perfil académico");
        }

        // 4. Obtener las materias permitidas para su grado.
        List<Materia> materiasDisponibles =
                materiaRepository.findActivasByGrado(
                        estudiante.getGradoEscolar());

        Set<Long> idsPermitidos = materiasDisponibles.stream()
                .map(Materia::getId)
                .collect(Collectors.toSet());

        // Convertir la selección en un conjunto para evitar
        // guardar materias duplicadas.
        Set<Long> idsSeleccionados =
                Set.copyOf(request.getMateriaIds());

        // 5. Validar que se haya seleccionado al menos una materia.
        if (idsSeleccionados.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar al menos una materia");
        }

        // 6. Comprobar que todas correspondan al grado escolar.
        if (!idsPermitidos.containsAll(idsSeleccionados)) {
            throw new IllegalArgumentException(
                    "Una o más materias no corresponden al grado del estudiante");
        }

        // 7. Consultar las preferencias del estudiante.
        PreferenciaAprendizaje preferencias =
                preferenciaAprendizajeRepository
                        .findByEstudianteId(estudiante.getId())
                        .orElse(null);

        // 8. Verificar los datos obligatorios y que exista
        // al menos una modalidad de aprendizaje seleccionada.
        boolean preferenciasCompletas = preferencias != null
                && preferencias.getNivelExplicacion() != null
                && preferencias.getNivelRetoPreferido() != null
                && preferencias.getRitmoEstudio() != null
                && (
                    preferencias.isPrefiereLecturaEscritura()
                    || preferencias.isPrefiereVisual()
                    || preferencias.isPrefiereAuditivo()
                    || preferencias.isPrefierePractica()
                );

        // 9. Reemplazar las materias anteriores únicamente
        // después de validar la nueva selección.
        estudianteMateriaRepository.eliminarPorEstudiante(
                estudiante.getId());

        for (Long materiaId : idsSeleccionados) {
            estudianteMateriaRepository.guardarSeleccion(
                    estudiante.getId(),
                    materiaId);
        }

        // 10. Recuperar las materias efectivamente guardadas.
        List<Materia> materiasGuardadas =
                estudianteMateriaRepository.findMateriasByEstudiante(
                        estudiante.getId());

        // 11. Marcar el onboarding como completado solamente
        // si el perfil, las preferencias y las materias
        // cumplen los requisitos.
        boolean perfilCompleto =
                estudiante.getEdad() != null
                && estudiante.getGradoEscolar() != null;

        if (perfilCompleto
                && preferenciasCompletas
                && !materiasGuardadas.isEmpty()) {

            estudiante.setOnboardingCompletado(true);

            // Guardar el estado en la misma transacción.
            estudianteRepository.save(estudiante);
        }

        // 12. Devolver las materias seleccionadas,
        // conservando el contrato actual del endpoint.
        return materiasGuardadas;
    }
    
        /**
         * Obtiene las materias seleccionadas por el estudiante
         * autenticado durante el onboarding.
         *
         * Este método se utilizará para mostrar las materias
         * reales del estudiante en su Dashboard.
         *
         * @param correo Correo del usuario autenticado.
         * @return Lista de materias seleccionadas.
         */
        @Transactional(readOnly = true)
        public List<Materia> obtenerMateriasSeleccionadas(String correo) {

        // 1. Identificar al usuario autenticado.
        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"));

        // 2. Recuperar su perfil de estudiante.
        Estudiante estudiante = estudianteRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Perfil de estudiante no encontrado"));

        // 3. Consultar las materias que tiene registradas.
        // Reutilizamos el repositorio existente.
        return estudianteMateriaRepository
                .findMateriasByEstudiante(estudiante.getId());
        }

}
