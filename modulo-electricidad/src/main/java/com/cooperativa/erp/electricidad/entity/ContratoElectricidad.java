package com.cooperativa.erp.electricidad.entity;

import com.cooperativa.erp.core.entity.ContratoServicio;
import com.cooperativa.erp.core.entity.Suministro;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad concreta para el servicio de Electricidad.
 * Hereda de ContratoServicio y añade los campos específicos.
 */
@Entity
@Table(name = "elec_contratos")
@PrimaryKeyJoinColumn(name = "contrato_id") // Une esta tabla con la tabla base usando el ID
@Data
@EqualsAndHashCode(callSuper = true) // Importante para incluir campos de la superclase en equals/hash
@NoArgsConstructor
public class ContratoElectricidad extends ContratoServicio {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medidor_id", nullable = false)
    private Medidor medidor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_tarifaria_id", nullable = false)
    private CategoriaTarifaria categoriaTarifaria;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoFase tipoFase;

    @Column(precision = 10, scale = 2)
    private BigDecimal potenciaContratadaKW;

    // --- Datos de Transformador (si aplica) ---
    private Boolean aplicaPerdidaTransformador = false;

    @Column(precision = 5, scale = 2) // Ej. 1.50 (para 1.50%)
    private BigDecimal porcentajePerdidaTransformador;


    public enum TipoFase {
        MONOFASICO,
        TRIFASICO
    }

    // Constructor
    public ContratoElectricidad(Suministro suministro, LocalDate fechaAlta,
                                Medidor medidor, CategoriaTarifaria categoriaTarifaria,
                                TipoFase tipoFase, BigDecimal potenciaContratadaKW) {

        // Llamamos al constructor de la clase padre
        super(suministro, ServicioTipo.ELECTRICIDAD, fechaAlta);

        this.medidor = medidor;
        this.categoriaTarifaria = categoriaTarifaria;
        this.tipoFase = tipoFase;
        this.potenciaContratadaKW = potenciaContratadaKW;
    }
}
