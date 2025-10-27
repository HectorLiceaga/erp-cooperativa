package com.cooperativa.erp.facturacion.entity;

// *** ¡IMPORT AÑADIDO/VERIFICADO! ***
import com.cooperativa.erp.electricidad.entity.ConceptoFacturable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Entity
@Table(name = "fac_factura_detalles", indexes = {
        @Index(name = "idx_facturadetalle_factura", columnList = "factura_id"),
        @Index(name = "idx_facturadetalle_concepto", columnList = "conceptoFacturable_id")
})
@Data // Asegura getters, setters, etc.
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura; // Relación bidireccional

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER) // Es útil traer el concepto al ver el detalle
    @JoinColumn(name = "conceptoFacturable_id", nullable = false)
    private ConceptoFacturable conceptoFacturable; // El concepto que se está facturando

    @NotBlank
    @Column(nullable = false, length = 255)
    private String descripcion; // Descripción detallada (puede incluir cantidad, precio unitario)

    @NotNull
    @Column(nullable = false, precision = 15, scale = 4) // Escala 4 para precios/cantidades precisas
    private BigDecimal cantidad = BigDecimal.ONE; // Por defecto 1 (para cargos fijos, etc.)

    @NotNull
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2) // Escala 2 para importes finales
    private BigDecimal importeNeto = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 5, scale = 2) // Ej: 21.00
    private BigDecimal alicuotaIVA = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importeIVA = BigDecimal.ZERO;

    // Podríamos tener campos para otros impuestos específicos del item

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importeTotal = BigDecimal.ZERO; // Neto + IVA + Otros Impuestos del Item
}

