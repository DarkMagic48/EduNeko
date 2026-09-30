package com.eduneko.dto;

import com.eduneko.entity.enums.NivelExplicacion;
import com.eduneko.entity.enums.NivelRetoPreferido;
import com.eduneko.entity.enums.RitmoEstudio;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public class ActualizarPreferenciasRequest {
    
    @NotNull(message = "El nivel de explicación es obligatorio")
    private NivelExplicacion nivelExplicacion;

    @NotNull(message = "El nivel de reto preferido es obligatorio")
    private NivelRetoPreferido nivelRetoPreferido;

    @NotNull(message = "El ritmo de estudio es obligatorio")
    private RitmoEstudio ritmoEstudio;

    private boolean prefiereLecturaEscritura;
    private boolean prefiereVisual;
    private boolean prefiereAuditivo;
    private boolean prefierePractica;


    @AssertTrue(message = "Debes seleccionar al menos una modalidad de aprendizaje")
    public boolean isAlgunaModalidadSeleccionada() {
        return prefiereLecturaEscritura
                || prefiereVisual
                || prefiereAuditivo
                || prefierePractica;
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
