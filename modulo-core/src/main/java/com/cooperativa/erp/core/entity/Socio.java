package com.cooperativa.erp.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "core_socios") // Prefijo 'core_' para tablas de este módulo
@Data
@NoArgsConstructor
public class Socio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombreCompleto; // O nombre y apellido separados

    @NotBlank
    @Column(unique = true, nullable = false, length = 11) // CUIT/CUIL suelen ser 11 dígitos
    private String cuitCuil;

    private String tipoDocumento; // DNI, LE, LC
    private String numeroDocumento;

    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;

    // Podríamos tener una relación a Domicilio aquí para el domicilio legal/postal
    // @ManyToOne
    // private Domicilio domicilioLegal;

    // Constructor útil
    public Socio(String nombreCompleto, String cuitCuil) {
        this.nombreCompleto = nombreCompleto;
        this.cuitCuil = cuitCuil;
    }
}

