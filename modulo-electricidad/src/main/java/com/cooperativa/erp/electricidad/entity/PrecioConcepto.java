package com.cooperativa.erp.electricidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "elec_precios_conceptos")
@Data
@NoArgsConstructor
public class PrecioConcepto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_tarifaria_id", nullable = false)
    private CategoriaTarifaria categoriaTarifaria;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concepto_facturable_id", nullable = false)
    private ConceptoFacturable conceptoFacturable;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaDesde; // Vigencia desde (inclusive)

    private LocalDate fechaHasta; // Vigencia hasta (exclusive). Null = indefinido

    @NotNull
    @Column(precision = 19, scale = 6) // Alta precisión para precios unitarios
    private BigDecimal precioUnitario;

    // Campos para precios escalonados (ej. primeros 100 KWh a un precio, excedente a otro)
    private BigDecimal limiteConsumoInferior; // Ej: 0 KWh
    private BigDecimal limiteConsumoSuperior; // Ej: 100 KWh (Null = sin límite superior)

    // Constructor útil
    public PrecioConcepto(CategoriaTarifaria categoriaTarifaria, ConceptoFacturable conceptoFacturable, LocalDate fechaDesde, BigDecimal precioUnitario) {
        this.categoriaTarifaria = categoriaTarifaria;
        this.conceptoFacturable = conceptoFacturable;
        this.fechaDesde = fechaDesde;
        this.precioUnitario = precioUnitario;
    }

    // Constructor para escalones
    public PrecioConcepto(CategoriaTarifaria categoriaTarifaria, ConceptoFacturable conceptoFacturable, LocalDate fechaDesde, BigDecimal precioUnitario, BigDecimal limiteInferior, BigDecimal limiteSuperior) {
        this(categoriaTarifaria, conceptoFacturable, fechaDesde, precioUnitario);
        this.limiteConsumoInferior = limiteInferior;
        this.limiteConsumoSuperior = limiteSuperior;
    }
}

