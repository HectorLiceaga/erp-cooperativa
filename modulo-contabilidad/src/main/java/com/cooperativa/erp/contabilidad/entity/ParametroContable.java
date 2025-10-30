package com.cooperativa.erp.contabilidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
// --- IMPORTACIÓN AÑADIDA ---
import jakarta.validation.constraints.NotNull;
// --- FIN IMPORTACIÓN ---
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad para parametrizar la contabilidad.
 * Mapea una "clave" de negocio (ej. "CTA_IVA_DEBITO_FISCAL")
 * a una cuenta contable específica (PlanDeCuentas).
 */
@Entity
@Table(name = "con_parametros_contables", indexes = {
        @Index(name = "idx_parametro_clave", columnList = "clave", unique = true)
})
@Data
@NoArgsConstructor
public class ParametroContable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String clave; // Ej: "CTA_VENTA_ENERGIA", "CTA_IVA_DEBITO", "CTA_CLIENTES_VENTA"

    @NotBlank
    @Column(nullable = false, length = 255)
    private String descripcion;

    @NotNull // <-- Este 'NotNull' necesitaba la importación
    @ManyToOne(fetch = FetchType.EAGER) // Queremos la cuenta al traer el parámetro
    @JoinColumn(name = "planDeCuentas_id", nullable = false)
    private PlanDeCuentas cuenta; // La cuenta contable asociada

    public ParametroContable(String clave, String descripcion, PlanDeCuentas cuenta) {
        this.clave = clave;
        this.descripcion = descripcion;
        this.cuenta = cuenta;
    }
}

