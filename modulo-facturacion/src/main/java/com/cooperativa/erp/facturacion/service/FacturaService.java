package com.cooperativa.erp.facturacion.service;

import com.cooperativa.erp.core.entity.Suministro;
import com.cooperativa.erp.facturacion.entity.Factura;
import com.cooperativa.erp.facturacion.entity.PuntoVenta;
import com.cooperativa.erp.facturacion.entity.ComprobanteTipo; // Import faltante
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Servicio para la generación y gestión de Facturas.
 */
public interface FacturaService {

    /**
     * Genera una factura para un suministro específico, correspondiente a un período de consumo.
     * Este método contendrá la lógica principal de cálculo.
     *
     * @param suministro El suministro a facturar.
     * @param fechaDesde Fecha de inicio del período de consumo.
     * @param fechaHasta Fecha de fin del período de consumo.
     * @param fechaVencimiento Fecha de vencimiento de la factura.
     * @param puntoVenta El punto de venta a utilizar para la numeración.
     * @return La Factura generada (aún sin persistir, o persistida según decidamos la lógica).
     * @throws Exception Si ocurre un error durante la generación (ej. no se encuentran lecturas, precios, etc.).
     */
    Factura generarFacturaPeriodo(Suministro suministro,
                                  LocalDate fechaDesde,
                                  LocalDate fechaHasta,
                                  LocalDate fechaVencimiento,
                                  PuntoVenta puntoVenta) throws Exception; // Usaremos excepciones más específicas luego

    /**
     * Obtiene el próximo número de comprobante disponible para un punto de venta y tipo de comprobante.
     *
     * @param puntoVenta El Punto de Venta.
     * @param comprobanteTipo El Tipo de Comprobante (ej. Factura A, Factura B).
     * @return El siguiente número a utilizar.
     */
    long obtenerProximoNumeroComprobante(PuntoVenta puntoVenta, ComprobanteTipo comprobanteTipo);

    /**
     * Guarda una factura generada en la base de datos y actualiza el último número del punto de venta.
     *
     * @param factura La factura a persistir.
     * @param puntoVenta El punto de venta utilizado (para actualizar su último número).
     * @return La factura persistida con su ID.
     */
    @Transactional
    Factura guardarFacturaYActualizarPuntoVenta(Factura factura, PuntoVenta puntoVenta);

    /**
     * Busca una factura por su ID.
     * @param id El ID de la factura.
     * @return Un Optional con la factura si se encuentra.
     */
    Optional<Factura> buscarPorId(Long id);

}
