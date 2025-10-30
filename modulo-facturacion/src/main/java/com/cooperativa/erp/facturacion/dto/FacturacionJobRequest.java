package com.cooperativa.erp.facturacion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para transportar los parámetros de ejecución
 * del Job de Facturación Masiva.
 */
@Data
public class FacturacionJobRequest {

    @NotNull(message = "El período a facturar no puede ser nulo.")
    private LocalDate periodo; // Ej: 2025-10-01 (Factura Octubre)

    @NotNull(message = "La fecha de vencimiento no puede ser nula.")
    private LocalDate fechaVencimiento; // Ej: 2025-11-15

    @NotNull(message = "El ID del Punto de Venta no puede ser nulo.")
    private Integer puntoVentaId; // El PV que emitirá las facturas
}
