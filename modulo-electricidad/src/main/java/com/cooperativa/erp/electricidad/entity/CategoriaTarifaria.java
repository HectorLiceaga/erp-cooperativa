package com.cooperativa.erp.electricidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "elec_categorias_tarifarias")
@Data
@NoArgsConstructor
public class CategoriaTarifaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String codigo; // Ej: "T1-R", "T2"

    @NotBlank
    @Column(nullable = false)
    private String descripcion; // Ej: "Tarifa 1 Residencial", "Tarifa 2 General"

    private boolean activa = true;

    public CategoriaTarifaria(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
}

