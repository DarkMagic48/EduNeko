
package com.eduneko.service;

import com.eduneko.dto.ContenidoCurricularResponse;
import com.eduneko.dto.DiagnosticoResultadoResponse;
import com.eduneko.dto.ProcesoAprendizajeResponse;
import com.eduneko.dto.TemaPriorizadoResponse;
import com.eduneko.dto.PlanAprendizajeResponse;
import com.eduneko.dto.PlanUnidadResponse;
import com.eduneko.dto.PlanPdaResponse;
import com.eduneko.repository.PlanAprendizajeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PlanAprendizajeService {

    private final DiagnosticoService diagnosticoService;
    private final PriorizacionCurricularService priorizacionService;
    private final PlanAprendizajeRepository planRepository;

    public PlanAprendizajeService(
            DiagnosticoService diagnosticoService,
            PriorizacionCurricularService priorizacionService,
            PlanAprendizajeRepository planRepository
    ) {
        this.diagnosticoService = diagnosticoService;
        this.priorizacionService = priorizacionService;
        this.planRepository = planRepository;
    }

        
        @Transactional
        public Long generarPlanMatematicas(String correo) {

        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);
                
        planRepository.bloquearEstudiante(estudianteId);

        DiagnosticoResultadoResponse resultado =
                diagnosticoService.obtenerResultadoMatematicas(correo);

        var planExistente = planRepository.buscarPlanActivo(
                estudianteId,
                resultado.materiaId(),
                resultado.gradoEscolar()
        );

        if (planExistente.isPresent()) {
                return planExistente.get();
        }

        Integer version = planRepository.obtenerSiguienteVersion(
                estudianteId,
                resultado.materiaId(),
                resultado.gradoEscolar()
        );

        return crearPlanPersonalizado(
                correo,
                estudianteId,
                resultado,
                version
        );
        }

        @Transactional
        public Long regenerarPlanMatematicas(String correo) {

        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);
                
        planRepository.bloquearEstudiante(estudianteId);

        DiagnosticoResultadoResponse resultado =
                diagnosticoService.obtenerResultadoMatematicas(correo);

        Long materiaId = resultado.materiaId();
        Short gradoEscolar = resultado.gradoEscolar();

        var planActual = planRepository.buscarPlanActivo(
                estudianteId,
                materiaId,
                gradoEscolar
        );

        if (planActual.isPresent()) {

                if (planRepository.tieneProgresoRegistrado(
                        planActual.get()
                )) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "No se puede regenerar un plan con progreso registrado"
                );
                }
        }

        Integer nuevaVersion =
                planRepository.obtenerSiguienteVersion(
                        estudianteId,
                        materiaId,
                        gradoEscolar
                );

        // Sustituir el plan anterior dentro de la misma transacción.
        planRepository.sustituirPlanActivo(
                estudianteId,
                materiaId,
                gradoEscolar
        );

        // Generar la nueva versión con la personalización actualizada.
        return crearPlanPersonalizado(
                correo,
                estudianteId,
                resultado,
                nuevaVersion
        );
        }
        
        private Long crearPlanPersonalizado(
                String correo,
                Long estudianteId,
                DiagnosticoResultadoResponse resultado,
                Integer version
        ) {

        List<TemaPriorizadoResponse> temas =
                priorizacionService.obtenerTemasPriorizados(correo);

        if (temas.isEmpty()) {
                throw new IllegalStateException(
                        "No existen temas priorizados para generar el plan"
                );
        }

        List<TemaPriorizadoResponse> temasSeleccionados =
                temas.stream()
                        .filter(tema -> tema.prioridad() <= 2)
                        .toList();

        // Si todos los temas son fortalezas observadas,
        // ofrecer una unidad de repaso orientativo.
        if (temasSeleccionados.isEmpty()) {
                temasSeleccionados = temas.stream()
                        .limit(1)
                        .toList();
        }

        Long planId = planRepository.crearPlan(
                estudianteId,
                resultado.materiaId(),
                resultado.gradoEscolar(),
                resultado.intentoId(),
                version
        );

        Set<Long> pdaAsignados = new HashSet<>();
        int ordenUnidad = 1;

        for (TemaPriorizadoResponse tema : temasSeleccionados) {

                String descripcion = tema.prioridad() == 1
                        ? "Unidad de refuerzo según el diagnóstico inicial."
                        : "Unidad de consolidación según el diagnóstico inicial.";

                String objetivo = tema.prioridad() == 1
                        ? "Reforzar los conocimientos fundamentales de "
                                + tema.tema() + "."
                        : "Consolidar los aprendizajes relacionados con "
                                + tema.tema() + ".";

                Long unidadId = planRepository.crearUnidad(
                        planId,
                        tema.tema(),
                        descripcion,
                        objetivo,
                        tema.tema(),
                        ordenUnidad,
                        tema.prioridad()
                );

                int pdaSeleccionados = 0;

                for (ContenidoCurricularResponse contenido :
                        tema.contenidosCurriculares()) {

                for (ProcesoAprendizajeResponse pda :
                        contenido.procesosAprendizaje()) {

                        if (pdaSeleccionados >= 3) {
                        break;
                        }

                        if (pdaAsignados.add(pda.id())) {
                        planRepository.vincularPda(
                                unidadId,
                                pda.id()
                        );

                        pdaSeleccionados++;
                        }
                }

                if (pdaSeleccionados >= 3) {
                        break;
                }
                }

                ordenUnidad++;
        }

        return planId;
        }

        @Transactional(readOnly = true)
        public PlanAprendizajeResponse obtenerPlanActualMatematicas(
                String correo
        ) {
        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        DiagnosticoResultadoResponse resultado =
                diagnosticoService.obtenerResultadoMatematicas(correo);

        Long planId = planRepository.buscarPlanActivo(
                estudianteId,
                resultado.materiaId(),
                resultado.gradoEscolar()
        ).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "El estudiante todavía no tiene un plan activo de Matemáticas"
        ));

        List<Map<String, Object>> filas =
                planRepository.obtenerUnidadesConPda(planId);

        Map<Long, UnidadTemporal> unidades = new LinkedHashMap<>();

        for (Map<String, Object> fila : filas) {

            Long unidadId = ((Number) fila.get("unidad_id")).longValue();

            UnidadTemporal unidad = unidades.get(unidadId);

            if (unidad == null) {
                unidad = new UnidadTemporal(
                        unidadId,
                        (String) fila.get("titulo"),
                        (String) fila.get("descripcion"),
                        (String) fila.get("objetivo"),
                        (String) fila.get("tema_diagnostico"),
                        ((Number) fila.get("orden")).intValue(),
                        ((Number) fila.get("prioridad")).intValue(),
                        (String) fila.get("estado"),
                        (BigDecimal) fila.get("progreso_porcentaje")
                );

                unidades.put(unidadId, unidad);
            }

            Object pdaId = fila.get("pda_id");

            if (pdaId != null) {
                unidad.pdas.add(new PlanPdaResponse(
                        ((Number) pdaId).longValue(),
                        (String) fila.get("pda_descripcion"),
                        ((Number) fila.get("pda_orden")).intValue()
                ));
            }
        }

        List<PlanUnidadResponse> unidadesResponse =
                unidades.values()
                        .stream()
                        .map(UnidadTemporal::toResponse)
                        .toList();

        
        Map<String, Object> datosPlan =
                planRepository.obtenerDatosPlan(planId);

        return new PlanAprendizajeResponse(
                planId,
                resultado.materiaId(),
                resultado.materiaNombre(),
                resultado.gradoEscolar(),
                (String) datosPlan.get("estado"),
                ((Number) datosPlan.get("version")).intValue(),
                unidadesResponse
        );

    }

    private static class UnidadTemporal {

        final Long id;
        final String titulo;
        final String descripcion;
        final String objetivo;
        final String temaDiagnostico;
        final Integer orden;
        final Integer prioridad;
        final String estado;
        final BigDecimal progreso;
        final List<PlanPdaResponse> pdas = new ArrayList<>();

        UnidadTemporal(
                Long id,
                String titulo,
                String descripcion,
                String objetivo,
                String temaDiagnostico,
                Integer orden,
                Integer prioridad,
                String estado,
                BigDecimal progreso
        ) {
            this.id = id;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.objetivo = objetivo;
            this.temaDiagnostico = temaDiagnostico;
            this.orden = orden;
            this.prioridad = prioridad;
            this.estado = estado;
            this.progreso = progreso;
        }

        PlanUnidadResponse toResponse() {
            return new PlanUnidadResponse(
                    id,
                    titulo,
                    descripcion,
                    objetivo,
                    temaDiagnostico,
                    orden,
                    prioridad,
                    estado,
                    progreso,
                    pdas
            );
        }
    }

}