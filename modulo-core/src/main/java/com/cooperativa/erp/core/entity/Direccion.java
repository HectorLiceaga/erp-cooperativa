package com.cooperativa.erp.core.entity; // Corregido

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Import para lat/lon si se descomentan

@Entity
@Table(name = "core_direcciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String calle;

    @Column(length = 20)
    private String numero; // String para casos como "S/N", "Km 5.5"

    @Column(length = 50)
    private String pisoDepto; // Ej: "3ro B"

    @NotBlank
    @Column(nullable = false, length = 100)
    private String localidad;

    @Column(length = 20)
    private String codigoPostal;

    @Column(length = 100)
    private String provincia; // Podría ser una entidad separada si necesitamos más detalle

    @Column(length = 100)
    private String pais = "Argentina"; // Valor por defecto

    @Column(length = 255)
    private String observaciones;

    // Podríamos añadir coordenadas GPS si fuera necesario
    // @Column(precision = 10, scale = 7)
    // private BigDecimal latitud;
    // @Column(precision = 10, scale = 7)
    // private BigDecimal longitud;

    // Constructor simplificado para casos comunes
    public Direccion(String calle, String numero, String localidad, String codigoPostal) {
        this.calle = calle;
        this.numero = numero;
        this.localidad = localidad;
        this.codigoPostal = codigoPostal;
    }
}
