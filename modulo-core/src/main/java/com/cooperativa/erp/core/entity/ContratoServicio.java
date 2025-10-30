package com.cooperativa.erp.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad base abstracta para un servicio contratado en un suministro.
 * Define los campos comunes a todos los servicios (Electricidad, Agua, Internet, etc.).
 * Usamos estrategia de herencia JOINED para separar los datos específicos
 * de cada tipo de servicio en su propia tabla.
 */
@Entity
@Table(name = "core_contratos_servicios")
@Inheritance(strategy = InheritanceType.JOINED) // Estrategia de Herencia
@Data
@NoArgsConstructor
public abstract class ContratoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suministro_id", nullable = false)
    private Suministro suministro;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServicioTipo tipoServicio; // Para saber qué tipo de contrato es

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaAlta;

    private LocalDate fechaBaja;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoContrato estado;

    public enum ServicioTipo {
        ELECTRICIDAD,
        AGUA,
        INTERNET,
        TELEFONIA,
        SEPELIO
    }

    public enum EstadoContrato {
        PENDIENTE,
        ACTIVO,
        SUSPENDIDO,
        BAJA
    }

    public ContratoServicio(Suministro suministro, ServicioTipo tipoServicio, LocalDate fechaAlta) {
        this.suministro = suministro;
        this.tipoServicio = tipoServicio;
        this.fechaAlta = fechaAlta;
        this.estado = EstadoContrato.ACTIVO;
    }
}
