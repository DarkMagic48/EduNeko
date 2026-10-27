package com.eduneko.controller;

import com.eduneko.dto.TemaPriorizadoResponse;
import com.eduneko.service.PriorizacionCurricularService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/priorizacion/matematicas")
public class PriorizacionCurricularController {

    private final PriorizacionCurricularService service;

    public PriorizacionCurricularController(
            PriorizacionCurricularService service
    ) {
        this.service = service;
    }

    @GetMapping("/temas")
    public List<TemaPriorizadoResponse> obtenerTemasPriorizados(
            Authentication authentication
    ) {
        return service.obtenerTemasPriorizados(
                authentication.getName()
        );
    }
}