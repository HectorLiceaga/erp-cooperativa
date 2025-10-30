package com.cooperativa.erp.electricidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "elec_conceptos_facturables")
@Data
@NoArgsConstructor
public class ConceptoFacturable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String codigo; // Ej: "CF", "KWH", "AP", "IVA21"

    @NotBlank
    @Column(nullable = false)
    private String descripcion; // Ej: "Cargo Fijo", "Consumo KWh", "Alumbrado Público", "IVA 21%"

    @NotNull
    @Enumerated(EnumType.STRING) // Para clasificar cómo se calcula o aplica
    private TipoConcepto tipo = TipoConcepto.FIJO;

    private boolean activo = true;

    // Podríamos agregar relaciones a cuentas contables aquí o en un módulo de configuración
    // private String cuentaContableVenta;
    // private String cuentaContableIva;

    public ConceptoFacturable(String codigo, String descripcion, TipoConcepto tipo) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.tipo = tipo;
    }

    public enum TipoConcepto {
        FIJO,        // Monto fijo por período
        VARIABLE,    // Depende del consumo (KWh)
        PORCENTAJE,  // Un % sobre otros conceptos (ej. impuestos)
        TASA         // Similar a FIJO, pero a menudo aplicado por unidad (ej. Alumbrado por frente)
        // Podríamos necesitar más tipos (ej. POR_POTENCIA)
    }
}

