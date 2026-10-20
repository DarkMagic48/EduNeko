package com.eduneko.controller;

import com.eduneko.dto.DiagnosticoInicioResponse;
import com.eduneko.service.DiagnosticoService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api/diagnosticos")
public class DiagnosticoController {
    
    private final DiagnosticoService diagnosticoService;

    public DiagnosticoController(
            DiagnosticoService diagnosticoService) {
        this.diagnosticoService = diagnosticoService;
    }

    @PostMapping("/matematicas/iniciar")
    public ResponseEntity<DiagnosticoInicioResponse> iniciarDiagnostico(
            Authentication authentication) {

        DiagnosticoInicioResponse resultado =
                diagnosticoService.iniciarDiagnostico(
                        authentication.getName());

        return ResponseEntity.ok(resultado);
    }
}
