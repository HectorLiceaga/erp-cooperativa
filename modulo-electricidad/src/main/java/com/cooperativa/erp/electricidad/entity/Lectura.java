package com.cooperativa.erp.electricidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "elec_lecturas")
@Data
@NoArgsConstructor
public class Lectura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY) // Varias lecturas por medidor
    @JoinColumn(name = "medidor_id", nullable = false)
    private Medidor medidor;

    @NotNull
    @Column(nullable = false)
    private LocalDate periodo; // Ej: 2025-10-01 (representa el mes/bimestre)

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaLectura;

    @NotNull
    @Column(precision = 12, scale = 2) // Ejemplo: 12 dígitos, 2 decimales
    private BigDecimal estadoAnterior;

    @NotNull
    @Column(precision = 12, scale = 2)
    private BigDecimal estadoActual;

    @NotNull
    @Column(precision = 12, scale = 2)
    private BigDecimal consumoCalculado; // (estadoActual - estadoAnterior) * constanteMultiplicacion

    private String tipoLectura; // Ej: "NORMAL", "ESTIMADA", "RECLAMO"
    private String lector; // Nombre o ID del operario

    // Constructor útil
    public Lectura(Medidor medidor, LocalDate periodo, LocalDateTime fechaLectura, BigDecimal estadoAnterior, BigDecimal estadoActual, String tipoLectura, String lector) {
        this.medidor = medidor;
        this.periodo = periodo;
        this.fechaLectura = fechaLectura;
        this.estadoAnterior = estadoAnterior;
        this.estadoActual = estadoActual;
        this.tipoLectura = tipoLectura;
        this.lector = lector;
        // Calcular el consumo (simplificado, habría que traer la constante del medidor)
        // En una implementación real, esto se haría en un Servicio.
        if(medidor != null && medidor.getConstanteMultiplicacion() != null) {
            this.consumoCalculado = estadoActual.subtract(estadoAnterior).multiply(medidor.getConstanteMultiplicacion());
        } else {
            this.consumoCalculado = estadoActual.subtract(estadoAnterior); // O lanzar error
        }
    }

    // Podríamos agregar un método prePersist/preUpdate para asegurar el cálculo del consumo
    @PrePersist
    @PreUpdate
    private void calcularConsumo() {
        if (medidor != null && medidor.getConstanteMultiplicacion() != null && estadoActual != null && estadoAnterior != null) {
            this.consumoCalculado = estadoActual.subtract(estadoAnterior).multiply(medidor.getConstanteMultiplicacion());
        } else if (estadoActual != null && estadoAnterior != null) {
            this.consumoCalculado = estadoActual.subtract(estadoAnterior); // Asumir KM=1 si no hay datos
        } else {
            this.consumoCalculado = BigDecimal.ZERO;
        }
    }
}


