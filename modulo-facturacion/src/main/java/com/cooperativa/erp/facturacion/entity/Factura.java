package com.cooperativa.erp.facturacion.entity;

import com.cooperativa.erp.core.entity.Socio;
import com.cooperativa.erp.core.entity.Suministro;
// --- IMPORTACIÓN AÑADIDA PARA EL BATCH ---
import com.cooperativa.erp.electricidad.entity.Lectura;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fac_facturas", indexes = {
        @Index(name = "idx_factura_socio", columnList = "socio_id"),
        @Index(name = "idx_factura_suministro", columnList = "suministro_id"),
        @Index(name = "idx_factura_pv_tipo_num", columnList = "puntoVenta_id, comprobanteTipo_id, numeroComprobante", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suministro_id", nullable = false)
    private Suministro suministro; // El suministro específico que se factura

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puntoVenta_id", nullable = false)
    private PuntoVenta puntoVenta;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER) // Traer siempre el tipo de comprobante
    @JoinColumn(name = "comprobanteTipo_id", nullable = false)
    private ComprobanteTipo comprobanteTipo;

    @NotNull
    @Column(nullable = false)
    private Long numeroComprobante; // El número correlativo dentro del PV y Tipo

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate fechaEmision;

    @NotNull
    @Column(nullable = false)
    private LocalDate periodoDesde;

    @NotNull
    @Column(nullable = false)
    private LocalDate periodoHasta;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importeNeto = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importeIVA = BigDecimal.ZERO;

    // Otros impuestos/percepciones podrían ir aquí o en detalles
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 15, scale = 2) // Corregido: 'false' no debe ir entre comillas
    private BigDecimal importeTotal = BigDecimal.ZERO;

    @NotNull
    @Column(nullable = false, length = 20)
    private String estado; // Ej: PENDIENTE, PAGADA, ANULADA

    // Detalles de la factura
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FacturaDetalle> detalles = new ArrayList<>();


    // --- CAMPO CLAVE AÑADIDO PARA EL BATCH ---
    /**
     * Almacena la Lectura "final" que disparó esta factura en el batch.
     * No se persiste en la BD (es @Transient), solo se usa en memoria
     * para pasarla del Processor (FacturacionProcessor) al Writer (FacturacionWriter).
     * El Writer la necesita para llamar a lecturaService.marcarLecturasComoFacturadas().
     */
    @Transient
    private Lectura lecturaFinal;


    // Métodos helper para manejar detalles (mantiene bidireccionalidad)
    public void addDetalle(FacturaDetalle detalle) {
        detalles.add(detalle);
        detalle.setFactura(this);
    }

    public void removeDetalle(FacturaDetalle detalle) {
        detalles.remove(detalle);
        detalle.setFactura(null);
    }

    // Podríamos añadir campos para CAE, Fecha Vto CAE, etc. para facturación electrónica
}

