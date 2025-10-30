package com.cooperativa.erp.electricidad.entity; // <-- PAQUETE CORREGIDO

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Entidad que representa una Lectura de medidor.
 * (Paquete corregido a com.cooperativa.erp.electricidad.entity)
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "elec_lecturas")
public class Lectura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * IMPORTANTE: Esta entidad (ContratoElectricidad) también debe estar
     * en el paquete 'com.cooperativa.erp.electricidad.entity'
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_electricidad_id", nullable = false)
    private ContratoElectricidad contratoElectricidad;

    @Column(nullable = false)
    private LocalDate fechaToma;

    @Column(nullable = false)
    private LocalDate fechaPeriodo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estadoActual;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estadoAnterior;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consumoKwh;

    private String tipoLectura; // "NORMAL", "ESTIMADA", "INFORMA_SOCIO"
    private boolean facturada = false;
}

