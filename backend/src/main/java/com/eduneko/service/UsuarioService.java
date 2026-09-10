package com.eduneko.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eduneko.entity.Usuario;
import com.eduneko.repository.UsuarioRepository;

@Service 
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String normalizarCorreo(String correo) {
        if (correo == null) {
            return null;
        }

        return correo.trim().toLowerCase(Locale.ROOT);
    }

    public boolean correoRegistrado(String correo) {
        String correoNormalizado = normalizarCorreo(correo);
        return usuarioRepository.existsByCorreo(correoNormalizado);
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        String correoNormalizado = normalizarCorreo(correo);
        return usuarioRepository.findByCorreo(correoNormalizado);
    }
}
