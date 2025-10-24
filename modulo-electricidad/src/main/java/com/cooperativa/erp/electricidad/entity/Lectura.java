package com.cooperativa.erp.electricidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor; // Mantenemos el constructor vacío para JPA

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "elec_lecturas")
@Data
@NoArgsConstructor // Lombok genera el constructor sin argumentos requerido por JPA
public class Lectura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Optional=false -> no puede ser nulo en BD
    @JoinColumn(name = "medidor_id", nullable = false)
    private Medidor medidor;

    @NotNull
    @PastOrPresent // La fecha no puede ser futura
    @Column(nullable = false)
    private LocalDate fechaLectura;

    @NotNull
    @PositiveOrZero // El estado no puede ser negativo
    @Column(nullable = false, precision = 10, scale = 2) // Ajusta precision/scale según necesidad
    private BigDecimal estado;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String tipoLectura; // Ej: "Normal", "Estimada", "Retiro", "Instalacion"

    // --- NUEVO CONSTRUCTOR ---
    /**
     * Constructor para crear una nueva lectura.
     * @param medidor El medidor asociado.
     * @param fechaLectura La fecha de la lectura.
     * @param estado El estado numérico leído.
     * @param tipoLectura El tipo de lectura.
     */
    public Lectura(Medidor medidor, LocalDate fechaLectura, BigDecimal estado, String tipoLectura) {
        this.medidor = medidor;
        this.fechaLectura = fechaLectura;
        this.estado = estado;
        this.tipoLectura = tipoLectura;
    }
    // --- FIN NUEVO CONSTRUCTOR ---

    // Podríamos añadir campos calculados si es útil, ej:
    // private BigDecimal consumoCalculado;
}

