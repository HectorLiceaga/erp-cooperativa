package com.cooperativa.erp.contabilidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa el Plan de Cuentas Contable.
 * Es una estructura de árbol (auto-referenciada).
 */
@Entity
@Table(name = "con_plan_de_cuentas", indexes = {
        @Index(name = "idx_cuenta_codigo", columnList = "codigo", unique = true),
        @Index(name = "idx_cuenta_padre", columnList = "padre_id")
})
@Data
@NoArgsConstructor
public class PlanDeCuentas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String codigo; // Ej: "1.01.01.001"

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String descripcion; // Ej: "Caja"

    @NotNull
    @Column(nullable = false)
    private Boolean imputable; // true si recibe asientos, false si es un título (cuenta padre)

    // Relación de árbol (Padre)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    private PlanDeCuentas padre;

    // Relación de árbol (Hijos)
    @OneToMany(mappedBy = "padre", fetch = FetchType.LAZY)
    private Set<PlanDeCuentas> hijos = new HashSet<>();

    // Podríamos añadir Nivel, Tipo (Activo, Pasivo, etc.), pero lo mantenemos simple por ahora.

    public PlanDeCuentas(String codigo, String descripcion, Boolean imputable, PlanDeCuentas padre) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.imputable = imputable;
        this.padre = padre;
    }
}
