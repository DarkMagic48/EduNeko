
package com.eduneko.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "diagnostico_resultado_tema",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_diagnostico_resultado_intento_tema",
            columnNames = {"intento_id", "tema"}
        )
    }
)
public class DiagnosticoResultadoTema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intento_id", nullable = false)
    private DiagnosticoIntento intento;

    @Column(name = "tema", nullable = false, length = 100)
    private String tema;

    @Column(name = "total_preguntas", nullable = false)
    private Integer totalPreguntas;

    @Column(name = "total_aciertos", nullable = false)
    private Integer totalAciertos;

    public DiagnosticoResultadoTema() {
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

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
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
