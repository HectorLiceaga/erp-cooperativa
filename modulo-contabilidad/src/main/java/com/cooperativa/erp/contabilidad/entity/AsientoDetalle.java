package com.cooperativa.erp.contabilidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Renglones (Debe/Haber) de un Asiento Contable.
 * Vinculado al Plan de Cuentas.
 */
@Entity
@Table(name = "con_asiento_detalles", indexes = {
        @Index(name = "idx_detalle_asiento", columnList = "asiento_id"),
        @Index(name = "idx_detalle_cuenta", columnList = "cuenta_id")
})
@Data
@NoArgsConstructor
public class AsientoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asiento_id", nullable = false)
    private Asiento asiento;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER) // Traer la cuenta es útil
    @JoinColumn(name = "cuenta_id", nullable = false)
    private PlanDeCuentas cuenta; // La cuenta imputable

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal debe = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal haber = BigDecimal.ZERO;

    public AsientoDetalle(Asiento asiento, PlanDeCuentas cuenta, BigDecimal debe, BigDecimal haber) {
        this.asiento = asiento;
        this.cuenta = cuenta;
        this.debe = debe;
        this.haber = haber;
    }
}
