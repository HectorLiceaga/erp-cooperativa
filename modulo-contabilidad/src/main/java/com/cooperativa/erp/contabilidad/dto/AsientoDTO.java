package com.cooperativa.erp.contabilidad.dto;

import com.cooperativa.erp.contabilidad.entity.Asiento;
import com.cooperativa.erp.contabilidad.entity.AsientoDetalle;
// --- IMPORTACIÓN AÑADIDA ---
import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
// --- IMPORTACIÓN AÑADIDA ---
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para la creación y visualización de Asientos Contables completos
 * (Cabecera y Detalles).
 */
@Data
@NoArgsConstructor
public class AsientoDTO {

    private Long id;

    @NotNull(message = "La fecha no puede ser nula")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate fecha;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 255)
    private String descripcion;

    // --- CAMPO AÑADIDO ---
    // (Para trazabilidad, ej: "modulo_facturacion", "carga_manual")
    @Size(max = 50)
    private String origen;
    // --- FIN CAMPO ---

    private BigDecimal totalDebe;
    private BigDecimal totalHaber;
    private String estado;

    @NotEmpty(message = "El asiento debe tener al menos un detalle")
    @Valid // Valida los DTOs anidados
    private List<AsientoDetalleDTO> detalles;

    /**
     * DTO anidado para los renglones del asiento.
     */
    @Data
    @NoArgsConstructor
    public static class AsientoDetalleDTO {
        private Long id;

        @NotBlank(message = "Se requiere un código de cuenta para el detalle")
        private String codigoCuenta; // Usamos el código (ej. "1.01.01.001") para imputar

        // --- CAMPO AÑADIDO ---
        @Size(max = 255)
        private String descripcion; // Descripción del renglón
        // --- FIN CAMPO ---

        @NotNull(message = "El Debe no puede ser nulo")
        @PositiveOrZero(message = "El Debe no puede ser negativo")
        private BigDecimal debe = BigDecimal.ZERO;

        @NotNull(message = "El Haber no puede ser nulo")
        @PositiveOrZero(message = "El Haber no puede ser negativo")
        private BigDecimal haber = BigDecimal.ZERO;

        // Constructor de Entity -> DTO
        public AsientoDetalleDTO(AsientoDetalle detalle) {
            this.id = detalle.getId();
            this.codigoCuenta = detalle.getCuenta().getCodigo();
            this.descripcion = detalle.getDescripcion(); // (Asegúrate que AsientoDetalle tenga descripcion)
            this.debe = detalle.getDebe();
            this.haber = detalle.getHaber();
        }
    }

    /**
     * Constructor de Entity -> DTO (para visualización)
     */
    public AsientoDTO(Asiento asiento) {
        this.id = asiento.getId();
        this.fecha = asiento.getFecha();
        this.descripcion = asiento.getDescripcion();
        this.origen = asiento.getOrigen(); // (Asegúrate que Asiento tenga origen)
        this.totalDebe = asiento.getTotalDebe();
        this.totalHaber = asiento.getTotalHaber();
        this.estado = asiento.getEstado();
        this.detalles = asiento.getDetalles().stream()
                .map(AsientoDetalleDTO::new)
                .collect(Collectors.toList());
    }

    // --- MÉTODO HELPER AÑADIDO (Clave para FacturaServiceImpl) ---
    /**
     * Helper para añadir renglones al DTO desde el servicio de facturación.
     */
    public void addDetalle(PlanDeCuentas cuenta, String descripcionRenglon, BigDecimal debe, BigDecimal haber) {
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }

        AsientoDetalleDTO detalleDTO = new AsientoDetalleDTO();
        detalleDTO.setCodigoCuenta(cuenta.getCodigo());
        detalleDTO.setDescripcion(descripcionRenglon);
        detalleDTO.setDebe(debe);
        detalleDTO.setHaber(haber);

        this.detalles.add(detalleDTO);
    }
    // --- FIN MÉTODO HELPER ---
}

