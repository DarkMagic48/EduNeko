package com.eduneko.entity;

import jakarta.persistence.*;

@Entity 
@Table(name = "diagnostico_pregunta")
public class DiagnosticoPregunta {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @Column(name = "grado_escolar", nullable = false)
    private Short gradoEscolar;

    @Column(name = "tema", nullable = false, length = 100)
    private String tema;

    @Column(name = "dificultad", nullable = false, length = 20)
    private String dificultad;

    @Column(name = "enunciado", nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public DiagnosticoPregunta() {
    }

    public Long getId() {
        return id;
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

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getDificultad() {
        return dificultad;
    }

    public void setDificultad(String dificultad) {
        this.dificultad = dificultad;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
