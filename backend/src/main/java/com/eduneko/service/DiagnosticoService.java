package com.eduneko.service;

import com.eduneko.entity.Usuario;
import com.eduneko.entity.Estudiante;
import com.eduneko.entity.DiagnosticoPregunta;
import com.eduneko.entity.DiagnosticoIntento;
import com.eduneko.entity.DiagnosticoOpcion;
import com.eduneko.entity.DiagnosticoRespuesta;
import com.eduneko.entity.DiagnosticoResultadoTema;
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
import com.eduneko.dto.FinalizarDiagnosticoRequest;
import com.eduneko.dto.DiagnosticoRespuestaRequest;
import com.eduneko.dto.DiagnosticoResultadoResponse;
import com.eduneko.dto.DiagnosticoResultadoTemaResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;

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

        @Transactional
        public DiagnosticoResultadoResponse finalizarDiagnostico(
                String correo,
                FinalizarDiagnosticoRequest request) {

        Estudiante estudiante = obtenerEstudianteAutenticado(correo);

        DiagnosticoIntento intento = intentoRepository
                .findByIdParaFinalizar(request.intentoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El intento de diagnóstico no existe"));

        if (!intento.getEstudiante().getId().equals(estudiante.getId())) {
                throw new IllegalArgumentException(
                        "El diagnóstico no pertenece al estudiante autenticado");
        }

        if (!"EN_PROGRESO".equals(intento.getEstado())) {
                throw new IllegalStateException(
                        "Este diagnóstico ya no está en progreso");
        }

        if (!intento.getGradoEscolar().equals(estudiante.getGradoEscolar())) {
                throw new IllegalStateException(
                        "El diagnóstico no corresponde al grado actual del estudiante");
        }

        if (!"Matemáticas".equalsIgnoreCase(intento.getMateria().getNombre())) {
                throw new IllegalArgumentException(
                        "El intento no corresponde a Matemáticas");
        }

        List<DiagnosticoPregunta> preguntas =
                obtenerPreguntasDiagnostico(estudiante);

        if (request.respuestas() == null || request.respuestas().size() != 15) {
                throw new IllegalArgumentException(
                        "Debes enviar exactamente 15 respuestas");
        }

        Map<Long, DiagnosticoPregunta> preguntasPorId = new HashMap<>();

        for (DiagnosticoPregunta pregunta : preguntas) {
                preguntasPorId.put(pregunta.getId(), pregunta);
        }

        Set<Long> preguntasRespondidas = new HashSet<>();
        List<DiagnosticoRespuesta> respuestasGuardar = new ArrayList<>();
        Map<String, int[]> resultadosPorTema = new HashMap<>();

        int totalAciertos = 0;

        for (DiagnosticoRespuestaRequest respuestaRequest : request.respuestas()) {

                if (respuestaRequest == null
                        || respuestaRequest.preguntaId() == null
                        || respuestaRequest.opcionId() == null) {
                throw new IllegalArgumentException(
                        "Todas las respuestas deben contener pregunta y opción");
                }

                Long preguntaId = respuestaRequest.preguntaId();

                if (!preguntasRespondidas.add(preguntaId)) {
                throw new IllegalArgumentException(
                        "No puedes responder dos veces la misma pregunta");
                }

                DiagnosticoPregunta pregunta = preguntasPorId.get(preguntaId);

                if (pregunta == null) {
                throw new IllegalArgumentException(
                        "La pregunta no pertenece a este diagnóstico");
                }

                if (respuestaRepository.existsByIntentoIdAndPreguntaId(
                        intento.getId(), preguntaId)) {
                throw new IllegalStateException(
                        "El intento ya contiene respuestas guardadas");
                }

                DiagnosticoOpcion opcion = opcionRepository
                        .findById(respuestaRequest.opcionId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "La opción seleccionada no existe"));

                if (!opcion.getPregunta().getId().equals(preguntaId)) {
                throw new IllegalArgumentException(
                        "La opción seleccionada no corresponde a la pregunta");
                }

                DiagnosticoRespuesta respuesta = new DiagnosticoRespuesta();
                respuesta.setIntento(intento);
                respuesta.setPregunta(pregunta);
                respuesta.setOpcion(opcion);
                respuestasGuardar.add(respuesta);

                int[] resultadoTema = resultadosPorTema.computeIfAbsent(
                        pregunta.getTema(), tema -> new int[2]);

                resultadoTema[0]++;

                if (opcion.isEsCorrecta()) {
                totalAciertos++;
                resultadoTema[1]++;
                }
        }

        if (preguntasRespondidas.size() != preguntas.size()) {
                throw new IllegalArgumentException(
                        "Debes responder todas las preguntas del diagnóstico");
        }

        if (resultadosPorTema.size() != 5) {
                throw new IllegalStateException(
                        "El diagnóstico debe contener exactamente cinco temas");
        }

        List<DiagnosticoResultadoTema> resultadosGuardar = new ArrayList<>();
        List<DiagnosticoResultadoTemaResponse> resultadosResponse =
                new ArrayList<>();

        resultadosPorTema.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {

                        String tema = entry.getKey();
                        int preguntasTema = entry.getValue()[0];
                        int aciertosTema = entry.getValue()[1];

                        if (preguntasTema != 3) {
                        throw new IllegalStateException(
                                "Cada tema debe contener exactamente tres preguntas");
                        }

                        DiagnosticoResultadoTema resultado =
                                new DiagnosticoResultadoTema();

                        resultado.setIntento(intento);
                        resultado.setTema(tema);
                        resultado.setTotalPreguntas(preguntasTema);
                        resultado.setTotalAciertos(aciertosTema);

                        resultadosGuardar.add(resultado);

                        double porcentaje =
                                Math.round((aciertosTema * 10000.0) / preguntasTema)
                                        / 100.0;

                        resultadosResponse.add(
                                new DiagnosticoResultadoTemaResponse(
                                        tema,
                                        preguntasTema,
                                        aciertosTema,
                                        porcentaje));
                });

        respuestaRepository.saveAll(respuestasGuardar);
        resultadoTemaRepository.saveAll(resultadosGuardar);

        intento.setTotalPreguntas(15);
        intento.setTotalAciertos(totalAciertos);
        intento.setFechaFin(LocalDateTime.now());
        intento.setEstado("FINALIZADO");

        intentoRepository.save(intento);

        double porcentajeGeneral =
                Math.round((totalAciertos * 10000.0) / 15) / 100.0;

        return new DiagnosticoResultadoResponse(
                intento.getId(),
                intento.getMateria().getId(),
                intento.getMateria().getNombre(),
                intento.getGradoEscolar(),
                intento.getEstado(),
                15,
                totalAciertos,
                porcentajeGeneral,
                resultadosResponse
        );
        
        }

        
        @Transactional(readOnly = true)
        public DiagnosticoResultadoResponse obtenerResultadoMatematicas(String correo) {

        Estudiante estudiante = obtenerEstudianteAutenticado(correo);

        Materia matematicas = estudianteMateriaRepository
                .findMateriasByEstudiante(estudiante.getId())
                .stream()
                .filter(materia ->
                        materia.getNombre().equalsIgnoreCase("Matemáticas"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "El estudiante no tiene Matemáticas seleccionada"));

        DiagnosticoIntento intento = intentoRepository
                .findByEstudianteIdOrderByFechaInicioDesc(estudiante.getId())
                .stream()
                .filter(i ->
                        i.getMateria().getId().equals(matematicas.getId())
                        && i.getGradoEscolar().equals(estudiante.getGradoEscolar())
                        && "FINALIZADO".equals(i.getEstado()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El estudiante todavía no ha finalizado el diagnóstico de Matemáticas"));

        List<DiagnosticoResultadoTemaResponse> resultadosPorTema =
                resultadoTemaRepository
                        .findByIntentoIdOrderByTemaAsc(intento.getId())
                        .stream()
                        .map(resultado -> {

                                double porcentaje = resultado.getTotalPreguntas() == 0
                                        ? 0.0
                                        : Math.round(
                                                resultado.getTotalAciertos() * 10000.0
                                                        / resultado.getTotalPreguntas()
                                        ) / 100.0;

                                return new DiagnosticoResultadoTemaResponse(
                                        resultado.getTema(),
                                        resultado.getTotalPreguntas(),
                                        resultado.getTotalAciertos(),
                                        porcentaje
                                );
                        })
                        .toList();

        int totalPreguntas = intento.getTotalPreguntas();
        int totalAciertos = intento.getTotalAciertos();

        double porcentajeGeneral = totalPreguntas == 0
                ? 0.0
                : Math.round(totalAciertos * 10000.0 / totalPreguntas) / 100.0;

        return new DiagnosticoResultadoResponse(
                intento.getId(),
                matematicas.getId(),
                matematicas.getNombre(),
                intento.getGradoEscolar(),
                intento.getEstado(),
                totalPreguntas,
                totalAciertos,
                porcentajeGeneral,
                resultadosPorTema
        );
    }

    @Transactional(readOnly = true)
    public Long obtenerEstudianteId(String correo) {
        return obtenerEstudianteAutenticado(correo).getId();    
    }

}
