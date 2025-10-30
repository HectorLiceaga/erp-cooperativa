package com.cooperativa.erp.contabilidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "con_asientos_detalles", indexes = {
        @Index(name = "idx_asientodetalle_cuenta", columnList = "cuenta_id")
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private PlanDeCuentas cuenta; // La cuenta imputable

    // --- CAMPO AÑADIDO (FASE 5) ---
    @Size(max = 255)
    private String descripcion; // Descripción del renglón
    // --- FIN CAMPO AÑADIDO ---

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal debe = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal haber = BigDecimal.ZERO;

    public AsientoDetalle(Asiento asiento, PlanDeCuentas cuenta, String descripcion, BigDecimal debe, BigDecimal haber) {
        this.asiento = asiento;
        this.cuenta = cuenta;
        this.descripcion = descripcion;
        this.debe = debe;
        this.haber = haber;
    }
}

