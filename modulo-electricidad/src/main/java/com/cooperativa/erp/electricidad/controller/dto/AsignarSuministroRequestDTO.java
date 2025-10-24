package com.cooperativa.erp.controller.electricidad.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para la solicitud de asignar un medidor a un suministro.
 */
@Data
public class AsignarSuministroRequestDTO {
    @NotNull(message = "El ID del suministro es requerido")
    private Long suministroId;
}


