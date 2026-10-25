package com.eduneko.controller;

import com.eduneko.dto.DiagnosticoTemaCurricularResponse;
import com.eduneko.service.DiagnosticoCurricularService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/curriculo/matematicas")
public class DiagnosticoCurricularController {

    private final DiagnosticoCurricularService service;

    public DiagnosticoCurricularController(
            DiagnosticoCurricularService service
    ) {
        this.service = service;
    }

    @GetMapping("/grados/{gradoEscolar}/temas")
    public DiagnosticoTemaCurricularResponse obtenerCurriculoPorTema(
            @PathVariable Integer gradoEscolar,
            @RequestParam String tema
    ) {
        return service.obtenerCurriculoPorTema(
                gradoEscolar,
                tema
        );
    }
}