package com.cooperativa.erp.facturacion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Define los tipos de comprobantes fiscales (Factura A, B, C, Nota Cred A, etc.)
 * y sus códigos AFIP asociados.
 */
@Entity
@Table(name = "fac_comprobante_tipos")
@Data
@NoArgsConstructor
public class ComprobanteTipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Usamos Integer porque los códigos AFIP son numéricos pequeños

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false, length = 50)
    private String descripcion; // Ej: "Factura A", "Nota de Crédito B"

    @NotBlank
    @Size(max = 3) // Los códigos AFIP suelen tener 2 o 3 dígitos
    @Column(unique = true, nullable = false, length = 3, name = "codigo_afip")
    private String codigoAfip;

    @NotBlank
    @Size(max = 1) // A, B, C, M, etc.
    @Column(nullable = false, length = 1, name = "letra")
    private String letra;

    // Podríamos añadir campos como 'signo_cta_cte' (+1 para facturas, -1 para notas de crédito)
    // O si discrimina IVA, etc.

    public ComprobanteTipo(String descripcion, String codigoAfip, String letra) {
        this.descripcion = descripcion;
        this.codigoAfip = codigoAfip;
        this.letra = letra;
    }
}

