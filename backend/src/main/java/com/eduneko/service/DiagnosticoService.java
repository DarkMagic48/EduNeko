package com.eduneko.service;

import com.eduneko.entity.Usuario;
import com.eduneko.entity.Estudiante;
import com.eduneko.entity.DiagnosticoPregunta;
import com.eduneko.entity.DiagnosticoIntento;
import com.eduneko.entity.DiagnosticoOpcion;
import com.eduneko.entity.Materia;
import com.eduneko.repository.UsuarioRepository;
import com.eduneko.repository.EstudianteRepository;
import com.eduneko.repository.EstudianteMateriaRepository;
import com.eduneko.repository.DiagnosticoPreguntaRepository;
import com.eduneko.repository.DiagnosticoOpcionRepository;
import com.eduneko.repository.DiagnosticoIntentoRepository;
import com.eduneko.repository.DiagnosticoRespuestaRepository;
import com.eduneko.repository.DiagnosticoResultadoTemaRepository;
import com.eduneko.dto.DiagnosticoInicioResponse;
import com.eduneko.dto.DiagnosticoPreguntaResponse;
import com.eduneko.dto.DiagnosticoOpcionResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service 
public class DiagnosticoService {
    
    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final EstudianteMateriaRepository estudianteMateriaRepository;
    private final DiagnosticoPreguntaRepository preguntaRepository;
    private final DiagnosticoOpcionRepository opcionRepository;
    private final DiagnosticoIntentoRepository intentoRepository;
    private final DiagnosticoRespuestaRepository respuestaRepository;
    private final DiagnosticoResultadoTemaRepository resultadoTemaRepository;

    public DiagnosticoService(
            UsuarioRepository usuarioRepository,
            EstudianteRepository estudianteRepository,
            EstudianteMateriaRepository estudianteMateriaRepository,
            DiagnosticoPreguntaRepository preguntaRepository,
            DiagnosticoOpcionRepository opcionRepository,
            DiagnosticoIntentoRepository intentoRepository,
            DiagnosticoRespuestaRepository respuestaRepository,
            DiagnosticoResultadoTemaRepository resultadoTemaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.estudianteMateriaRepository = estudianteMateriaRepository;
        this.preguntaRepository = preguntaRepository;
        this.opcionRepository = opcionRepository;
        this.intentoRepository = intentoRepository;
        this.respuestaRepository = respuestaRepository;
        this.resultadoTemaRepository = resultadoTemaRepository;
    }

    private Estudiante obtenerEstudianteAutenticado(String correo) {

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

        return estudiante;
    }

    
    private void validarMatematicasSeleccionada(Estudiante estudiante) {

        boolean matematicasSeleccionada =
                estudianteMateriaRepository
                        .findMateriasByEstudiante(estudiante.getId())
                        .stream()
                        .anyMatch(materia ->
                                materia.getNombre()
                                        .equalsIgnoreCase("Matemáticas"));

        if (!matematicasSeleccionada) {
            throw new IllegalStateException(
                    "El estudiante debe seleccionar Matemáticas " +
                    "antes de iniciar el diagnóstico");
        }
    }

    
    private List<DiagnosticoPregunta> obtenerPreguntasDiagnostico(Estudiante estudiante) {

        validarMatematicasSeleccionada(estudiante);

        Materia matematicas = estudianteMateriaRepository
                .findMateriasByEstudiante(estudiante.getId())
                .stream()
                .filter(materia ->
                        materia.getNombre()
                                .equalsIgnoreCase("Matemáticas"))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Matemáticas no está seleccionada"));

        List<DiagnosticoPregunta> preguntas =
                preguntaRepository
                        .findByMateriaIdAndGradoEscolarAndActivoTrueOrderByIdAsc(
                                matematicas.getId(),
                                estudiante.getGradoEscolar()
                        );

        if (preguntas.size() != 15) {
            throw new IllegalStateException(
                    "El diagnóstico de Matemáticas no está disponible " +
                    "para este grado escolar. Se requieren 15 preguntas " +
                    "activas y se encontraron " + preguntas.size());
        }

        return preguntas;
    }

    
        private DiagnosticoIntento obtenerOCrearIntento(
                Estudiante estudiante,
                Materia materia) {

        boolean diagnosticoFinalizado = intentoRepository
                .findByEstudianteIdOrderByFechaInicioDesc(
                        estudiante.getId())
                .stream()
                .anyMatch(intento ->
                        intento.getMateria().getId().equals(materia.getId())
                        && intento.getGradoEscolar()
                                .equals(estudiante.getGradoEscolar())
                        && "FINALIZADO".equals(intento.getEstado())
                );

        if (diagnosticoFinalizado) {
                throw new IllegalStateException(
                        "Ya completaste el diagnóstico inicial de " +
                        materia.getNombre() +
                        " para tu grado escolar. " +
                        "Puedes consultar tus resultados.");
        }

        Optional<DiagnosticoIntento> intentoExistente =
                intentoRepository
                        .findFirstByEstudianteIdAndMateriaIdAndEstadoOrderByFechaInicioDesc(
                                estudiante.getId(),
                                materia.getId(),
                                "EN_PROGRESO"
                        );

        if (intentoExistente.isPresent()) {
                DiagnosticoIntento intento = intentoExistente.get();

                if (!intento.getGradoEscolar()
                        .equals(estudiante.getGradoEscolar())) {
                throw new IllegalStateException(
                        "Existe un diagnóstico en progreso de otro grado escolar");
                }

                return intento;
        }

        DiagnosticoIntento nuevoIntento = new DiagnosticoIntento();

        nuevoIntento.setEstudiante(estudiante);
        nuevoIntento.setMateria(materia);
        nuevoIntento.setGradoEscolar(estudiante.getGradoEscolar());
        nuevoIntento.setEstado("EN_PROGRESO");

        return intentoRepository.save(nuevoIntento);
        }


    
        @Transactional
        public DiagnosticoInicioResponse iniciarDiagnostico(String correo) {

                Estudiante estudiante = obtenerEstudianteAutenticado(correo);

                List<DiagnosticoPregunta> preguntas =
                        obtenerPreguntasDiagnostico(estudiante);

                Materia materia = preguntas.get(0).getMateria();

                List<DiagnosticoPreguntaResponse> preguntasResponse =
                        preguntas.stream()
                                .map(pregunta -> {

                                List<DiagnosticoOpcion> opciones =
                                        opcionRepository
                                                .findByPreguntaIdOrderByIdAsc(
                                                        pregunta.getId());

                                if (opciones.size() != 4) {
                                        throw new IllegalStateException(
                                                "La pregunta " + pregunta.getId() +
                                                " debe tener exactamente 4 opciones");
                                }

                                long correctas = opciones.stream()
                                        .filter(DiagnosticoOpcion::isEsCorrecta)
                                        .count();

                                if (correctas != 1) {
                                        throw new IllegalStateException(
                                                "La pregunta " + pregunta.getId() +
                                                " debe tener exactamente una respuesta correcta");
                                }

                                List<DiagnosticoOpcionResponse> opcionesResponse =
                                        opciones.stream()
                                                .map(opcion ->
                                                        new DiagnosticoOpcionResponse(
                                                                opcion.getId(),
                                                                opcion.getTexto()))
                                                .collect(Collectors.toList());

                                return new DiagnosticoPreguntaResponse(
                                        pregunta.getId(),
                                        pregunta.getTema(),
                                        pregunta.getDificultad(),
                                        pregunta.getEnunciado(),
                                        opcionesResponse
                                );
                                })
                                .collect(Collectors.toList());

                DiagnosticoIntento intento =
                        obtenerOCrearIntento(estudiante, materia);

                return new DiagnosticoInicioResponse(
                        intento.getId(),
                        materia.getId(),
                        materia.getNombre(),
                        estudiante.getGradoEscolar(),
                        intento.getEstado(),
                        preguntasResponse
                );
        }

}
