package com.eduneko.service;

import com.eduneko.dto.DiagnosticoResultadoResponse;
import com.eduneko.dto.DiagnosticoResultadoTemaResponse;
import com.eduneko.dto.TemaPriorizadoResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Comparator;

@Service
public class PriorizacionCurricularService {

    private final DiagnosticoService diagnosticoService;
    private final DiagnosticoCurricularService curricularService;

    public PriorizacionCurricularService(
            DiagnosticoService diagnosticoService,
            DiagnosticoCurricularService curricularService
    ) {
        this.diagnosticoService = diagnosticoService;
        this.curricularService = curricularService;
    }

    public DiagnosticoResultadoResponse obtenerResultadoDiagnostico(
            String correo
    ) {
        return diagnosticoService.obtenerResultadoMatematicas(correo);
    }

    private String determinarNivelDesempeno(
        Integer totalAciertos,
        Integer totalPreguntas
    ) {

    if (totalAciertos == null
            || totalPreguntas == null
            || totalPreguntas <= 0
            || totalAciertos < 0
            || totalAciertos > totalPreguntas) {
        throw new IllegalArgumentException(
                "Los resultados del tema no son válidos"
        );
    }

    double proporcion = (double) totalAciertos / totalPreguntas;

    if (proporcion <= 1.0 / 3.0) {
        return "REQUIERE_REFUERZO";
    }

    if (proporcion < 1.0) {
        return "EN_DESARROLLO";
    }

    return "FORTALEZA_OBSERVADA";
    }

    private Integer determinarPrioridad(String nivelDesempeno) {

        return switch (nivelDesempeno) {
            case "REQUIERE_REFUERZO" -> 1;
            case "EN_DESARROLLO" -> 2;
            case "FORTALEZA_OBSERVADA" -> 3;
            default -> throw new IllegalArgumentException(
                    "Nivel de desempeño no reconocido"
            );
        };
    }

    private TemaPriorizadoResponse construirTemaPriorizado(
        DiagnosticoResultadoTemaResponse resultadoTema,
        Integer gradoEscolar
    ) {

    String nivelDesempeno = determinarNivelDesempeno(
            resultadoTema.totalAciertos(),
            resultadoTema.totalPreguntas()
    );

    Integer prioridad = determinarPrioridad(nivelDesempeno);

    var respuestaCurricular = curricularService.obtenerCurriculoPorTema(
            gradoEscolar,
            resultadoTema.tema()
    );

    return new TemaPriorizadoResponse(
            resultadoTema.tema(),
            resultadoTema.totalPreguntas(),
            resultadoTema.totalAciertos(),
            resultadoTema.porcentajeAciertos(),
            nivelDesempeno,
            prioridad,
            respuestaCurricular.contenidosCurriculares()
        );
    }

    public List<TemaPriorizadoResponse> obtenerTemasPriorizados(
        String correo
    ) {

    DiagnosticoResultadoResponse resultado =
            diagnosticoService.obtenerResultadoMatematicas(correo);

    Integer gradoEscolar = resultado.gradoEscolar().intValue();

    return resultado.resultadosPorTema()
            .stream()
            .map(tema -> construirTemaPriorizado(
                    tema,
                    gradoEscolar
            ))
            .sorted(
                    Comparator.comparing(
                            TemaPriorizadoResponse::prioridad
                    ).thenComparing(
                            TemaPriorizadoResponse::totalAciertos
                    ).thenComparing(
                            TemaPriorizadoResponse::tema
                    )
            )
            .toList();
    }
}