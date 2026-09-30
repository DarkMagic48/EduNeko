package com.eduneko.entity;

import com.eduneko.entity.enums.NivelExplicacion;
import com.eduneko.entity.enums.NivelRetoPreferido;
import com.eduneko.entity.enums.RitmoEstudio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "preferencia_aprendizaje")
public class PreferenciaAprendizaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false, unique = true)
    private Estudiante estudiante;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_explicacion", nullable = false)
    private NivelExplicacion nivelExplicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_reto_preferido", nullable = false)
    private NivelRetoPreferido nivelRetoPreferido;

    @Enumerated(EnumType.STRING)
    @Column(name = "ritmo_estudio", nullable = false)
    private RitmoEstudio ritmoEstudio;

    @Column(name = "prefiere_lectura_escritura", nullable = false)
    private boolean prefiereLecturaEscritura;

    @Column(name = "prefiere_visual", nullable = false)
    private boolean prefiereVisual;

    @Column(name = "prefiere_auditivo", nullable = false)
    private boolean prefiereAuditivo;

    @Column(name = "prefiere_practica", nullable = false)
    private boolean prefierePractica;

    public PreferenciaAprendizaje() {
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

    public NivelExplicacion getNivelExplicacion() {
        return nivelExplicacion;
    }

    public void setNivelExplicacion(NivelExplicacion nivelExplicacion) {
        this.nivelExplicacion = nivelExplicacion;
    }

    public NivelRetoPreferido getNivelRetoPreferido() {
        return nivelRetoPreferido;
    }

    public void setNivelRetoPreferido(NivelRetoPreferido nivelRetoPreferido) {
        this.nivelRetoPreferido = nivelRetoPreferido;
    }

    public RitmoEstudio getRitmoEstudio() {
        return ritmoEstudio;
    }

    public void setRitmoEstudio(RitmoEstudio ritmoEstudio) {
        this.ritmoEstudio = ritmoEstudio;
    }

    public boolean isPrefiereLecturaEscritura() {
        return prefiereLecturaEscritura;
    }

    public void setPrefiereLecturaEscritura(boolean prefiereLecturaEscritura) {
        this.prefiereLecturaEscritura = prefiereLecturaEscritura;
    }

    public boolean isPrefiereVisual() {
        return prefiereVisual;
    }

    public void setPrefiereVisual(boolean prefiereVisual) {
        this.prefiereVisual = prefiereVisual;
    }

    public boolean isPrefiereAuditivo() {
        return prefiereAuditivo;
    }

    public void setPrefiereAuditivo(boolean prefiereAuditivo) {
        this.prefiereAuditivo = prefiereAuditivo;
    }

    public boolean isPrefierePractica() {
        return prefierePractica;
    }

    public void setPrefierePractica(boolean prefierePractica) {
        this.prefierePractica = prefierePractica;
    }
}

