package com.eduneko.entity;

import jakarta.persistence.*;

@Entity 
@Table(name = "diagnostico_opcion")
public class DiagnosticoOpcion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private DiagnosticoPregunta pregunta;

    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta = false;

    public DiagnosticoOpcion() {
    }

    public Long getId() {
        return id;
    }

    public DiagnosticoPregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(DiagnosticoPregunta pregunta) {
        this.pregunta = pregunta;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public boolean isEsCorrecta() {
        return esCorrecta;
    }

    public void setEsCorrecta(boolean esCorrecta) {
        this.esCorrecta = esCorrecta;
    }
}
