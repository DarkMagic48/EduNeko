package com.eduneko.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api/usuario")
public class UsuarioController {

    @GetMapping("/me")
    public Map<String, String> obtenerUsuarioActual(
            Authentication authentication) {

        return Map.of(
                "correo", authentication.getName());
    }
}
