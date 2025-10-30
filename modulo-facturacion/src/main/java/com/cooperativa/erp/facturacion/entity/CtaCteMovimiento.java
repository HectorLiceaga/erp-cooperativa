package com.cooperativa.erp.facturacion.entity;

import com.cooperativa.erp.core.entity.Socio;
import com.cooperativa.erp.core.entity.Suministro;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa un movimiento individual en la cuenta corriente de un Socio.
 * (Débitos por facturas, Créditos por pagos o notas de crédito).
 */
@Entity
@Table(name = "fac_ctacte_movimientos", indexes = {
        @Index(name = "idx_ctacte_socio_fecha", columnList = "socio_id, fechaContable"),
        @Index(name = "idx_ctacte_factura", columnList = "factura_id"),
        @Index(name = "idx_ctacte_suministro", columnList = "suministro_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CtaCteMovimiento {

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
    private Suministro suministro;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate fechaContable; // Fecha en que impacta en la CtaCte

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate fechaEmision; // Fecha del comprobante que lo origina

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipoMovimiento; // DEBE, HABER

    @NotNull
    @Column(nullable = false, length = 100)
    private String concepto; // Ej: "Factura de Servicios", "Pago", "Nota de Crédito"

    // Relación con la factura (DEBE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", unique = true) // Un movimiento de débito por factura
    private Factura factura;

    // TODO: Relación con el Pago o Nota de Crédito (HABER)
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "pago_id")
    // private Pago pago;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importe = BigDecimal.ZERO; // El valor del movimiento (siempre positivo)

    // El saldo se calculará en tiempo real, no se almacena aquí
    // para evitar inconsistencias.

    public enum TipoMovimiento {
        DEBE, // Suma a la deuda (Facturas, Notas de Débito)
        HABER // Resta a la deuda (Pagos, Notas de Crédito)
    }

    // Constructor para un DEBE por Factura
    public CtaCteMovimiento(Factura factura) {
        this.socio = factura.getSocio();
        this.suministro = factura.getSuministro();
        this.fechaContable = factura.getFechaEmision(); // O la fecha contable si se define
        this.fechaEmision = factura.getFechaEmision();
        this.tipoMovimiento = TipoMovimiento.DEBE;
        this.concepto = String.format("%s %s %04d-%08d",
                factura.getComprobanteTipo().getDescripcion(),
                factura.getComprobanteTipo().getLetra(),
                factura.getPuntoVenta().getNumero(),
                factura.getNumeroComprobante()
        );
        this.factura = factura;
        this.importe = factura.getImporteTotal();
    }
}
