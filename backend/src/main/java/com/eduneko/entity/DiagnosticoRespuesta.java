package com.eduneko.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "diagnostico_respuesta",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_diagnostico_respuesta_intento_pregunta",
            columnNames = {"intento_id", "pregunta_id"}
        )
    }
)
public class DiagnosticoRespuesta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intento_id", nullable = false)
    private DiagnosticoIntento intento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private DiagnosticoPregunta pregunta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opcion_id", nullable = false)
    private DiagnosticoOpcion opcion;

    public DiagnosticoRespuesta() {
    }

    public Long getId() {
        return id;
    }

    public DiagnosticoIntento getIntento() {
        return intento;
    }

    public void setIntento(DiagnosticoIntento intento) {
        this.intento = intento;
    }

    public DiagnosticoPregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(DiagnosticoPregunta pregunta) {
        this.pregunta = pregunta;
    }

    public DiagnosticoOpcion getOpcion() {
        return opcion;
    }

    public void setOpcion(DiagnosticoOpcion opcion) {
        this.opcion = opcion;
    }
}
