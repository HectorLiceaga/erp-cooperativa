package com.cooperativa.erp.contabilidad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data; // <-- ANOTACIÓN AÑADIDA
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "con_plan_de_cuentas", indexes = {
        @Index(name = "idx_planctas_codigo", columnList = "codigo", unique = true)
})
@Data // <-- ASEGURARSE DE QUE ESTÉ PRESENTE
@NoArgsConstructor
public class PlanDeCuentas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)*$", message = "El código debe seguir un formato numérico separado por puntos (ej. 1.01.01.001)")
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    // Relación de árbol (auto-referencia)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_padre_id")
    private PlanDeCuentas cuentaPadre;

    @OneToMany(mappedBy = "cuentaPadre", fetch = FetchType.LAZY)
    private Set<PlanDeCuentas> cuentasHijas = new HashSet<>();

    @NotNull
    @Column(nullable = false)
    private Boolean imputable = false; // <-- TRUE si es una cuenta de último nivel (ej. "Caja")
    // FALSE si es una cuenta de agrupación (ej. "Activo")
}

