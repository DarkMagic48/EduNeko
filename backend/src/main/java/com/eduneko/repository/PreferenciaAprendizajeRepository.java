package com.eduneko.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduneko.entity.PreferenciaAprendizaje;

public interface PreferenciaAprendizajeRepository extends JpaRepository<PreferenciaAprendizaje, Long> {

    Optional<PreferenciaAprendizaje> findByEstudianteId(Long estudianteId);
}
