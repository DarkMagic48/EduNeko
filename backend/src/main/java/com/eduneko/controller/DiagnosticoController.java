package com.eduneko.controller;

import com.eduneko.dto.DiagnosticoInicioResponse;
import com.eduneko.dto.FinalizarDiagnosticoRequest;
import com.eduneko.dto.DiagnosticoResultadoResponse;
import com.eduneko.service.DiagnosticoService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

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

    @PostMapping("/matematicas/finalizar")
    public ResponseEntity<DiagnosticoResultadoResponse> finalizarDiagnostico(
            Authentication authentication,
            @Valid @RequestBody FinalizarDiagnosticoRequest request) {

        DiagnosticoResultadoResponse resultado =
                diagnosticoService.finalizarDiagnostico(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/matematicas/resultado")
        public ResponseEntity<DiagnosticoResultadoResponse> obtenerResultadoMatematicas(
                Authentication authentication) {

        return ResponseEntity.ok(
                diagnosticoService.obtenerResultadoMatematicas(
                        authentication.getName()
                )
        );
    }
}
