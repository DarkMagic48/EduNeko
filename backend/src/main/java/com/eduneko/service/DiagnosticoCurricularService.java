package com.eduneko.service;

import com.eduneko.dto.DiagnosticoTemaCurricularResponse;
import com.eduneko.repository.DiagnosticoCurricularRepository;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticoCurricularService {

    private final DiagnosticoCurricularRepository repository;

    public DiagnosticoCurricularService(
            DiagnosticoCurricularRepository repository
    ) {
        this.repository = repository;
    }

    public DiagnosticoTemaCurricularResponse obtenerCurriculoPorTema(
            Integer gradoEscolar,
            String temaDiagnostico
    ) {

        if (gradoEscolar == null
                || gradoEscolar < 1
                || gradoEscolar > 3) {
            throw new IllegalArgumentException(
                    "El grado escolar debe estar entre 1 y 3."
            );
        }

        if (temaDiagnostico == null
                || temaDiagnostico.isBlank()) {
            throw new IllegalArgumentException(
                    "El tema del diagnóstico es obligatorio."
            );
        }

        var contenidos = repository.obtenerContenidosPorTema(
                gradoEscolar,
                temaDiagnostico
        );

        return new DiagnosticoTemaCurricularResponse(
                gradoEscolar,
                temaDiagnostico,
                contenidos
        );
    }
}