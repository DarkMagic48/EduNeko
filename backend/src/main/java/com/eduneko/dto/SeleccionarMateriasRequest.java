package com.eduneko.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SeleccionarMateriasRequest {
    
    @NotEmpty(message = "Debes seleccionar al menos una materia")
    private List<@NotNull(message = "El ID de la materia no puede ser nulo")Long> materiaIds;

    public List<Long> getMateriaIds() {
        return materiaIds;
    }

    public void setMateriaIds(List<Long> materiaIds) {
        this.materiaIds = materiaIds;
    }
}
