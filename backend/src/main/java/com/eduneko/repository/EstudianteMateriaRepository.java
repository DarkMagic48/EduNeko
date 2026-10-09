package com.eduneko.repository;

import com.eduneko.entity.Materia;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EstudianteMateriaRepository extends Repository<Materia, Long> {
    
    @Modifying
    @Query(value = """
            DELETE FROM estudiante_materia
            WHERE estudiante_id = :estudianteId
            """, nativeQuery = true)
    void eliminarPorEstudiante(
            @Param("estudianteId") Long estudianteId);

    @Modifying
    @Query(value = """
            INSERT INTO estudiante_materia (estudiante_id, materia_id)
            VALUES (:estudianteId, :materiaId)
            """, nativeQuery = true)
    void guardarSeleccion(
            @Param("estudianteId") Long estudianteId,
            @Param("materiaId") Long materiaId);

    @Query(value = """
            SELECT m.*
            FROM materia m
            INNER JOIN estudiante_materia em
                ON em.materia_id = m.id
            WHERE em.estudiante_id = :estudianteId
            ORDER BY m.id
            """, nativeQuery = true)
    List<Materia> findMateriasByEstudiante(
            @Param("estudianteId") Long estudianteId);
}
