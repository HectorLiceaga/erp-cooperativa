package com.cooperativa.erp.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // Importar NotNull
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "core_socios")
@Data
@NoArgsConstructor
public class Socio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombreCompleto;

    @NotBlank
    @Column(unique = true, nullable = false, length = 11)
    private String cuitCuil;

    private String tipoDocumento;
    private String numeroDocumento;

    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;

    // --- NUEVO CAMPO ---
    @NotNull // Es un dato obligatorio para facturar
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CondicionIVA condicionIVA = CondicionIVA.CONSUMIDOR_FINAL; // Valor por defecto
    // --- FIN NUEVO CAMPO ---


    // Constructor útil
    public Socio(String nombreCompleto, String cuitCuil) {
        this.nombreCompleto = nombreCompleto;
        this.cuitCuil = cuitCuil;
    }

    // --- NUEVO ENUM ---
    public enum CondicionIVA {
        RESPONSABLE_INSCRIPTO,
        MONOTRIBUTO,
        CONSUMIDOR_FINAL,
        EXENTO,
        NO_RESPONSABLE // (Podría haber otras específicas de Argentina)
    }
    // --- FIN NUEVO ENUM ---
}

