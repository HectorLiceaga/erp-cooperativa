package com.cooperativa.erp.contabilidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera del Asiento Contable.
 */
@Entity
@Table(name = "con_asientos", indexes = {
        @Index(name = "idx_asiento_fecha", columnList = "fecha")
})
@Data
@NoArgsConstructor
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate fecha;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String descripcion; // Ej: "Facturación Período 10/2025"

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDebe = BigDecimal.ZERO;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalHaber = BigDecimal.ZERO;

    @NotNull
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE"; // PENDIENTE, CONFIRMADO, ANULADO

    // Relación con los renglones (Detalle)
    @OneToMany(mappedBy = "asiento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AsientoDetalle> detalles = new ArrayList<>();

    // Métodos helper para la bidireccionalidad
    public void addDetalle(AsientoDetalle detalle) {
        detalles.add(detalle);
        detalle.setAsiento(this);
        // Actualizar totales (simplificado)
        if (detalle.getDebe() != null) {
            this.totalDebe = this.totalDebe.add(detalle.getDebe());
        }
        if (detalle.getHaber() != null) {
            this.totalHaber = this.totalHaber.add(detalle.getHaber());
        }
    }
}
