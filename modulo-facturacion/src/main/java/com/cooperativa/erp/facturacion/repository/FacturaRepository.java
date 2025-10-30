package com.cooperativa.erp.facturacion.repository;

import com.cooperativa.erp.facturacion.entity.Factura;
import com.cooperativa.erp.facturacion.entity.PuntoVenta;
import com.cooperativa.erp.facturacion.entity.ComprobanteTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    /**
     * Busca el último número de comprobante utilizado para un punto de venta
     * y tipo de comprobante específicos. Es crucial para obtener el siguiente número.
     * Se ordena descendentemente por numeroComprobante y se toma el primero (top 1).
     */
    Optional<Factura> findTopByPuntoVentaAndComprobanteTipoOrderByNumeroComprobanteDesc(
            PuntoVenta puntoVenta, ComprobanteTipo comprobanteTipo);

    // Podríamos añadir más métodos de búsqueda útiles, por ejemplo:
    // List<Factura> findBySocioIdAndFechaEmisionBetween(Long socioId, LocalDate desde, LocalDate hasta);
}

