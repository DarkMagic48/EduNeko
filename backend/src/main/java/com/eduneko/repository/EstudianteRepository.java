package com.eduneko.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eduneko.entity.Estudiante;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByUsuarioId(Long usuarioId);
}
