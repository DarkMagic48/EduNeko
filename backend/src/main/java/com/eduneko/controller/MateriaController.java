package com.eduneko.controller;

import com.eduneko.entity.Materia;
import com.eduneko.service.MateriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController 
@RequestMapping("/api/materias")
public class MateriaController {
    
    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public ResponseEntity<List<Materia>> obtenerMateriasDisponibles(
            Authentication authentication) {

        List<Materia> materias =
                materiaService.obtenerMateriasDisponibles(
                        authentication.getName());

        return ResponseEntity.ok(materias);
    }

}
