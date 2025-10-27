package com.cooperativa.erp.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "core_socios")
@Data // Asegura getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class Socio {

    public enum CondicionIVA {
        RESPONSABLE_INSCRIPTO,
        MONOTRIBUTO,
        EXENTO,
        CONSUMIDOR_FINAL,
        NO_CATEGORIZADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombreCompleto;

    @NotBlank
    @Size(min = 11, max = 11) // CUIT/CUIL Argentino tiene 11 dígitos
    @Column(nullable = false, unique = true, length = 11)
    private String cuit; // Añadido campo CUIT

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CondicionIVA condicionIVA = CondicionIVA.CONSUMIDOR_FINAL;

    // Relación OneToMany con Suministro (un Socio puede tener varios suministros)
    // mappedBy indica que la gestión de la FK está en la entidad Suministro (campo 'socio')
    // CascadeType.ALL: Si borro un Socio, se borran sus Suministros (¡Cuidado! Quizás no queremos esto siempre)
    // orphanRemoval=true: Si quito un suministro de esta lista, se borra de la BD.
    @OneToMany(mappedBy = "socio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Suministro> suministros = new ArrayList<>();

    // Método helper para añadir suministros (mantiene la bidireccionalidad)
    public void addSuministro(Suministro suministro) {
        suministros.add(suministro);
        suministro.setSocio(this);
    }

    // Método helper para remover suministros
    public void removeSuministro(Suministro suministro) {
        suministros.remove(suministro);
        suministro.setSocio(null);
    }
}


