package com.cooperativa.erp.facturacion.service;

import com.cooperativa.erp.core.entity.Suministro;
import com.cooperativa.erp.electricidad.entity.Lectura;
import com.cooperativa.erp.facturacion.entity.ComprobanteTipo;
import com.cooperativa.erp.facturacion.entity.Factura;
import com.cooperativa.erp.facturacion.entity.PuntoVenta;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Interfaz (API Interna) para la lógica de negocio de Facturación.
 * ACTUALIZADO: Añadido método para ser llamado por Spring Batch.
 */
public interface FacturaService {

    /**
     * Genera un objeto Factura (en memoria) para un Suministro y rango de fechas.
     * Útil para facturación individual/manual.
     */
    Factura generarFacturaPeriodo(Suministro suministro,
                                  LocalDate fechaDesde,
                                  LocalDate fechaHasta,
                                  LocalDate fechaVencimiento,
                                  PuntoVenta puntoVenta) throws Exception;

    /**
     * Genera un objeto Factura (en memoria) a partir de la lectura final del período.
     * Es el método principal que usará el Spring Batch.
     */
    Factura generarFacturaDesdeLectura(Lectura lecturaFinal,
                                       LocalDate fechaVencimiento,
                                       PuntoVenta puntoVenta) throws Exception;

    /**
     * Guarda la factura en la BD, asigna número de comprobante, actualiza el PV
     * y (a futuro) genera el movimiento en CtaCte.
     * Es transaccional y bloquea el Punto de Venta.
     */
    Factura guardarFacturaYActualizarPuntoVenta(Factura factura, PuntoVenta puntoVenta);

    /**
     * Obtiene el próximo número de comprobante para un PV y Tipo.
     * Es transaccional y bloquea el Punto de Venta.
     */
    long obtenerProximoNumeroComprobante(PuntoVenta puntoVenta, ComprobanteTipo comprobanteTipo);


    Optional<Factura> buscarPorId(Long id);
}
