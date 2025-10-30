package com.cooperativa.erp.facturacion.entity;

import com.cooperativa.erp.electricidad.entity.ConceptoFacturable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "fac_factura_detalles", indexes = {
        @Index(name = "idx_detalle_factura", columnList = "factura_id"),
        @Index(name = "idx_detalle_concepto", columnList = "concepto_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER) // Traer el concepto
    @JoinColumn(name = "concepto_id", nullable = false)
    private ConceptoFacturable conceptoFacturable;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String descripcion; // Descripción del item (ej. "Cargo Fijo", "Consumo 100 kWh @ 50.00")

    @NotNull
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal cantidad;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal precioUnitario;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importeNeto;

    // TODO: Añadir campos para alícuota de IVA, importe de IVA, etc.
}

