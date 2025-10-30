package com.cooperativa.erp.core.entity;

import com.cooperativa.erp.core.entity.Direccion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.util.ArrayList; // Importar ArrayList
import java.util.List; // Importar List

@Entity
@Table(name = "core_suministros", uniqueConstraints = {
        @UniqueConstraint(name = "uk_suministro_nis", columnNames = {"nis"})
}, indexes = {
        @Index(name = "idx_suministro_socio", columnList = "socio_id"),
        @Index(name = "idx_suministro_direccion", columnList = "direccion_id"),
        @Index(name = "idx_suministro_ruta_folio", columnList = "ruta, folio")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Suministro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String nis;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "direccion_id", nullable = false)
    private Direccion direccion;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate fechaAlta;

    @PastOrPresent
    private LocalDate fechaBaja;

    @Column(length = 255)
    private String razonBaja;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSuministro estado = EstadoSuministro.PENDIENTE_CONEXION;

    // --- Datos Logísticos ---
    @Column
    private Integer ruta;

    @Column
    private Integer folio;

    @Column(length = 100)
    private String zona;

    // --- Flags / Datos Específicos ---
    private Boolean bonificacionBomberos = false;
    private Boolean bonificacionLuzYFuerza = false;
    private Boolean exentoCorteCapital = false;
    private Boolean tarifaCero = false;
    private Boolean violenciaGenero = false;
    private Boolean titularFallecido = false;
    private LocalDate fechaFallecido;
    private Boolean noEnviarAvisosCorte = false;

    // --- NUEVA RELACIÓN ---
    /**
     * Un suministro puede tener múltiples contratos de servicios (luz, agua, etc.)
     * 'mappedBy = "suministro"' indica que la entidad ContratoServicio
     * es la dueña de la relación (ella tiene el @ManyToOne).
     */
    @OneToMany(mappedBy = "suministro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ContratoServicio> contratos = new ArrayList<>();

    // --- Enums y Constructores ---
    public enum EstadoSuministro {
        PENDIENTE_CONEXION,
        ACTIVO,
        SUSPENDIDO_FALTA_PAGO,
        CORTADO_FALTA_PAGO,
        SUSPENDIDO_VOLUNTARIO,
        BAJA_VOLUNTARIA,
        BAJA_POR_DEUDA,
        INACTIVO
    }

    public Suministro(String nis, Socio socio, Direccion direccion, LocalDate fechaAlta) {
        this.nis = nis;
        this.socio = socio;
        this.direccion = direccion;
        this.fechaAlta = fechaAlta;
        this.estado = EstadoSuministro.ACTIVO;
    }

    // --- Métodos Helper para Contratos ---
    public void addContrato(ContratoServicio contrato) {
        if(this.contratos == null) {
            this.contratos = new ArrayList<>();
        }
        this.contratos.add(contrato);
        contrato.setSuministro(this);
    }

    public void removeContrato(ContratoServicio contrato) {
        if(this.contratos != null) {
            this.contratos.remove(contrato);
        }
        contrato.setSuministro(null);
    }
}

