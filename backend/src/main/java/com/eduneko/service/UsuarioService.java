package com.eduneko.service;

import java.util.Locale;
import java.util.Optional;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eduneko.entity.Usuario;
import com.eduneko.repository.UsuarioRepository;
import com.eduneko.dto.RegistroUsuarioRequest;
import com.eduneko.entity.Estudiante;
import com.eduneko.repository.EstudianteRepository;

@Service 
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, EstudianteRepository estudianteRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.passwordEncoder = passwordEncoder;
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

    public  String generarHashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public  boolean passwordCoincide(String password, String hash) {
        return passwordEncoder.matches(password, hash);
    }

    @Transactional 
    public Usuario registrarUsuario(RegistroUsuarioRequest request) {

        if (!request.getPassword().equals(request.getConfirmarPassword())) {
        throw new IllegalArgumentException(
                "Las contraseñas no coinciden");
    }

    String correoNormalizado = normalizarCorreo(request.getCorreo());

    if (usuarioRepository.existsByCorreo(correoNormalizado)) {
        throw new IllegalArgumentException(
                "El correo ya está registrado");
    }

    Usuario usuario = new Usuario();
    usuario.setNombre(request.getNombre().trim());
    usuario.setCorreo(correoNormalizado);
    usuario.setPasswordHash(
            generarHashPassword(request.getPassword()));

    Usuario usuarioGuardado = usuarioRepository.save(usuario);

    Estudiante estudiante = new Estudiante();
    estudiante.setUsuario(usuarioGuardado);

    estudianteRepository.save(estudiante);

    return usuarioGuardado;

    }

    public Usuario autenticar(String correo, String password) {

    String correoNormalizado = normalizarCorreo(correo);

    Usuario usuario = usuarioRepository
            .findByCorreo(correoNormalizado)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Correo o contraseña incorrectos"));

    if (!usuario.isActivo()) {
        throw new IllegalArgumentException(
                "Correo o contraseña incorrectos");
    }

    if (!passwordCoincide(password, usuario.getPasswordHash())) {
        throw new IllegalArgumentException(
                "Correo o contraseña incorrectos");
    }

    return usuario;
    }
}
