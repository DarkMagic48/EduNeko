package com.eduneko.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity 
@Table(name = "diagnostico_intento")
public class DiagnosticoIntento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @Column(name = "grado_escolar", nullable = false)
    private Short gradoEscolar;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "EN_PROGRESO";

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "total_preguntas")
    private Integer totalPreguntas;

    @Column(name = "total_aciertos")
    private Integer totalAciertos;

    public DiagnosticoIntento() {
    }

    @PrePersist
    public void asignarFechaInicio() {
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public Short getGradoEscolar() {
        return gradoEscolar;
    }

    public void setGradoEscolar(Short gradoEscolar) {
        this.gradoEscolar = gradoEscolar;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getTotalPreguntas() {
        return totalPreguntas;
    }

    public void setTotalPreguntas(Integer totalPreguntas) {
        this.totalPreguntas = totalPreguntas;
    }

    public Integer getTotalAciertos() {
        return totalAciertos;
    }

    public void setTotalAciertos(Integer totalAciertos) {
        this.totalAciertos = totalAciertos;
    }
}
