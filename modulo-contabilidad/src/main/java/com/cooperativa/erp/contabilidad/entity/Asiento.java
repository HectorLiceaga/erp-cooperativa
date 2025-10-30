package com.cooperativa.erp.contabilidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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
import java.util.Objects;

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
    private String descripcion;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDebe = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalHaber = BigDecimal.ZERO;

    @NotNull
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String estado; // Ej: "BORRADOR", "CONFIRMADO", "ANULADO"

    // --- CAMPO AÑADIDO (FASE 5) ---
    @Size(max = 50)
    @Column(length = 50)
    private String origen; // Ej: "modulo_facturacion", "carga_manual"
    // --- FIN CAMPO AÑADIDO ---

    @OneToMany(mappedBy = "asiento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AsientoDetalle> detalles = new ArrayList<>();

    // ... (Helpers) ...

    /**
     * Helper para validación de cuadratura
     */
    public boolean estaBalanceado() {
        return totalDebe.compareTo(totalHaber) == 0;
    }

    /**
     * Helper para sincronizar bidireccionalidad y recalcular totales
     */
    public void addDetalle(AsientoDetalle detalle) {
        detalles.add(detalle);
        detalle.setAsiento(this);
        recalcularTotales();
    }

    public void removeDetalle(AsientoDetalle detalle) {
        detalles.remove(detalle);
        detalle.setAsiento(null);
        recalcularTotales();
    }

    /**
     * Recalcula los totales del asiento en base a sus detalles.
     */
    public void recalcularTotales() {
        this.totalDebe = detalles.stream()
                .map(AsientoDetalle::getDebe)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalHaber = detalles.stream()
                .map(AsientoDetalle::getHaber)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

