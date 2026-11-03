package com.eduneko.service;

import com.eduneko.repository.ProgresoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;

@Service
public class ProgresoService {

    private final ProgresoRepository progresoRepository;
    private final LeccionService leccionService;
    private final DiagnosticoService diagnosticoService;

    public ProgresoService(
            ProgresoRepository progresoRepository,
            LeccionService leccionService,
            DiagnosticoService diagnosticoService) {

        this.progresoRepository = progresoRepository;
        this.leccionService = leccionService;
        this.diagnosticoService = diagnosticoService;
    }

    @Transactional(readOnly = true)
    public ProgresoLeccionResponse obtenerProgresoLeccion(
            Long unidadId,
            String correo) {

        // Validar que la unidad pertenece al estudiante.
        Map<String, Object> leccion =
                leccionService.obtenerLeccion(unidadId, correo);

        Long leccionId =
                ((Number) leccion.get("id")).longValue();

        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        // Consultar los intentos registrados.
        Map<String, Object> datos =
                progresoRepository.obtenerProgresoLeccion(
                        leccionId,
                        estudianteId
                );

        int totalActividades =
                ((Number) datos.get("total_actividades")).intValue();

        int actividadesCorrectas =
                ((Number) datos.get("actividades_correctas")).intValue();

        int puntosObtenidos =
                ((Number) datos.get("puntos_obtenidos")).intValue();

        // Calcular el porcentaje de avance.
        int progresoPorcentaje = totalActividades == 0
                ? 0
                : (int) Math.round(
                        actividadesCorrectas * 100.0 / totalActividades
                );

        // Una lección se completa cuando todas sus
        // actividades han sido respondidas correctamente.
        boolean completada =
                totalActividades > 0
                && actividadesCorrectas == totalActividades;

        return new ProgresoLeccionResponse(
                leccionId,
                unidadId,
                totalActividades,
                actividadesCorrectas,
                progresoPorcentaje,
                puntosObtenidos,
                completada
        );
    }

        /**
         * Obtiene el total de puntos acumulados por el estudiante.
         *
         * El correo proviene del usuario autenticado y permite
         * identificar al estudiante sin recibir su ID desde
         * el frontend.
         *
         * Los puntos se calculan a partir de los intentos
         * registrados en PostgreSQL.
         */
        @Transactional(readOnly = true)
        public int obtenerPuntosTotales(String correo) {

        // Identificar al estudiante autenticado.
        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        // Consultar los puntos acumulados en todas sus lecciones.
        return progresoRepository.obtenerPuntosTotales(
                estudianteId
        );
        }

        /**
         * Consulta las rachas de aprendizaje del estudiante autenticado.
         * Una racha representa los días consecutivos en los que el estudiante realizó al menos una actividad evaluada.
         * La información se calcula desde los intentos guardados en PostgreSQL, sin necesidad de almacenar contadores.
         */
        @Transactional(readOnly = true)
        public RachasResponse obtenerRachas(String correo) {

        // Identificar al estudiante mediante su correo autenticado.
        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        // Consultar las rachas calculadas por PostgreSQL.
        Map<String, Object> datos =
                progresoRepository.obtenerRachas(estudianteId);

        // Convertir los valores numéricos devueltos por PostgreSQL.
        int rachaActual =
                ((Number) datos.get("racha_actual")).intValue();

        int rachaMaxima =
                ((Number) datos.get("racha_maxima")).intValue();

        // Construir la respuesta que posteriormente recibirá Angular.
        return new RachasResponse(
                rachaActual,
                rachaMaxima
        );
        }

        /**
         * Estructura de respuesta para las rachas de aprendizaje.
         */
        public record RachasResponse(
                int rachaActual,
                int rachaMaxima
        ) {
        }

        @Transactional(readOnly = true)
        public LogrosResponse obtenerLogros(String correo) {

        // Identificar al estudiante autenticado.
        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        // Comprobar si respondió correctamente alguna actividad.
        boolean primerAcierto =
                progresoRepository.tienePrimerAcierto(estudianteId);

        // Comprobar si completó al menos una lección.
        boolean primeraLeccion =
                progresoRepository.tieneLeccionCompletada(estudianteId);

        // Consultar los puntos XP acumulados.
        int puntosTotales =
                progresoRepository.obtenerPuntosTotales(estudianteId);

        // Consultar la racha máxima histórica.
        Map<String, Object> rachas =
                progresoRepository.obtenerRachas(estudianteId);

        int rachaMaxima =
                ((Number) rachas.get("racha_maxima")).intValue();

        // Construir las cuatro insignias de EduNeko.
        List<LogroResponse> logros = List.of(

                new LogroResponse(
                "PRIMER_ACIERTO",
                "Primer acierto",
                "Responde correctamente tu primera actividad.",
                primerAcierto
                ),

                new LogroResponse(
                "PRIMERA_LECCION",
                "Primera lección",
                "Completa tu primera lección en EduNeko.",
                primeraLeccion
                ),

                new LogroResponse(
                "APRENDIZ_DESTACADO",
                "Aprendiz destacado",
                "Acumula 50 puntos XP.",
                puntosTotales >= 50
                ),

                new LogroResponse(
                "CONSTANCIA",
                "Constancia",
                "Alcanza una racha de 3 días consecutivos.",
                rachaMaxima >= 3
                )
        );

        // Devolver el catálogo con los estados de desbloqueo.
        return new LogrosResponse(logros);
        }

        public record LogroResponse(
                String codigo,
                String nombre,
                String descripcion,
                boolean desbloqueado
        ) {}

        /**
         * Respuesta que agrupa el catálogo de logros.
         */
        public record LogrosResponse(
                List<LogroResponse> logros
        ) {}

    public record ProgresoLeccionResponse(
            Long leccionId,
            Long unidadId,
            int totalActividades,
            int actividadesCorrectas,
            int progresoPorcentaje,
            int puntosObtenidos,
            boolean completada
    ) {
    }
}
