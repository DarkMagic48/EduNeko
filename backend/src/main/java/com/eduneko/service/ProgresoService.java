package com.eduneko.service;

import com.eduneko.repository.ProgresoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
