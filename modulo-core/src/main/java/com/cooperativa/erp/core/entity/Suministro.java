package com.cooperativa.erp.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "core_suministros") // También con prefijo 'core_'
@Data
@NoArgsConstructor
public class Suministro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Un suministro pertenece a UN socio (titular)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @NotBlank
    @Column(nullable = false)
    private String direccionCompleta; // Calle, número, piso, depto, etc.

    // Podríamos normalizar la dirección en una entidad separada Domicilio
    // private String calle;
    // private String numero;
    // @ManyToOne
    // private Localidad localidad;

    private String identificadorUnico; // Ej: NIS, Número de Suministro, etc. (puede ser único por cooperativa)

    private LocalDate fechaAlta;
    private String estado = "ACTIVO"; // ACTIVO, INACTIVO, PENDIENTE_ALTA

    // Constructor útil
    public Suministro(Socio socio, String direccionCompleta) {
        this.socio = socio;
        this.direccionCompleta = direccionCompleta;
        this.fechaAlta = LocalDate.now();
    }
}

