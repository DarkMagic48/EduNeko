package com.eduneko.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduneko.dto.RegistroUsuarioRequest;
import com.eduneko.dto.RegistroUsuarioResponse;
import com.eduneko.dto.LoginRequest;
import com.eduneko.dto.LoginResponse;
import com.eduneko.entity.Usuario;
import com.eduneko.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

     private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistroUsuarioResponse> registrar(
            @Valid @RequestBody RegistroUsuarioRequest request) {

        Usuario usuario = usuarioService.registrarUsuario(request);

        RegistroUsuarioResponse response =
                new RegistroUsuarioResponse(
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getCorreo(),
                        "Usuario registrado correctamente");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        
        Usuario usuario = usuarioService.autenticar(
                request.getCorreo(), 
                request.getPassword());

        LoginResponse response = 
                new LoginResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), "Inicio de sesión correcto");
        
        return ResponseEntity.ok(response);
    }
    
}
