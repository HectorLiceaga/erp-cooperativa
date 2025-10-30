package com.cooperativa.erp.core.entity;

import com.cooperativa.erp.core.entity.Direccion; // Importar Direccion
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "core_socios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_socio_cuit", columnNames = {"cuit"})
        // Podríamos añadir uk_socio_dni si el DNI también debe ser único
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Socio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nombreCompleto;

    @NotBlank
    @Column(nullable = false, unique = true, length = 13) // CUIT/CUIL
    private String cuit;

    @Column(length = 10) // DNI u otro identificador
    private String documentoNumero; // Añadido DNI

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CondicionIVA condicionIVA = CondicionIVA.CONSUMIDOR_FINAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PersonaTipo personaTipo = PersonaTipo.FISICA; // Añadido FISICA o JURIDICA

    @PastOrPresent
    private LocalDate fechaIngreso; // Añadido Fecha de Ingreso

    @NotNull
    @Column(nullable = false)
    private Boolean activo = true; // Campo activo (antes 'enabled' en Usuario)

    // --- Flags específicos ---
    private Boolean esGubernamental = false; // Añadido flag
    private Boolean esProvincial = false; // Añadido flag
    private Boolean esEmpleadoCooperativa = false; // Añadido flag
    private Boolean esInquilino = false; // Añadido flag (antes 'soloTitular')
    private Boolean enviarFacturaMail = false; // Añadido flag

    // --- Datos Proveedor (si aplica) ---
    private Boolean esProveedor = false; // Añadido flag
    @Column(length = 20)
    private String numeroProveedor; // Añadido número proveedor
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CondicionGanancias condicionGanancias; // Añadido Condición Ganancias
    @Column(length = 100)
    private String regimenGanancias; // Añadido Régimen Ganancias

    // --- Contacto (simple por ahora) ---
    @Column(length = 50)
    private String telefono; // Añadido teléfono
    @Column(length = 100)
    private String email; // Añadido email

    // --- Direcciones ---
    // Un socio puede tener múltiples direcciones (fiscal, postal, etc.)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id") // Columna en la tabla 'core_direcciones' que apunta a Socio
    private List<Direccion> direcciones = new ArrayList<>(); // Cambiado a Lista de Direccion

    // --- Capital Cooperativo (si aplica) ---
    @Column(precision = 15, scale = 2)
    private BigDecimal capitalAportado; // Añadido campo capital


    // --- Enums Internos ---
    public enum CondicionIVA {
        RESPONSABLE_INSCRIPTO,
        MONOTRIBUTO,
        EXENTO,
        NO_RESPONSABLE,
        CONSUMIDOR_FINAL
    }

    public enum PersonaTipo {
        FISICA,
        JURIDICA
    }

    public enum CondicionGanancias {
        INSCRIPTO,
        NO_INSCRIPTO,
        EXENTO, // Exento de retención
        NO_ALCANZADO // No corresponde retención
        // Podrían faltar otras condiciones específicas
    }

    // --- Métodos Helper para Direcciones ---
    public void addDireccion(Direccion direccion) {
        this.direcciones.add(direccion);
        // No necesitamos setSocio(this) porque usamos @JoinColumn unidireccional aquí
    }

    public void removeDireccion(Direccion direccion) {
        this.direcciones.remove(direccion);
    }

    // Constructor simplificado (opcional, Lombok genera @AllArgsConstructor)
    public Socio(String nombreCompleto, String cuit, CondicionIVA condicionIVA) {
        this.nombreCompleto = nombreCompleto;
        this.cuit = cuit;
        this.condicionIVA = condicionIVA;
    }
}

