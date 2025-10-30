package com.cooperativa.erp.facturacion.service;

import com.cooperativa.erp.facturacion.entity.Factura;
import com.cooperativa.erp.facturacion.entity.CtaCteMovimiento;

/**
 * Servicio para gestionar la Lógica de Negocio de la Cuenta Corriente.
 */
public interface CtaCteService {

    /**
     * Registra el movimiento DEBE (débito) correspondiente a una factura
     * recién guardada.
     * Este método debe ser llamado DENTRO de la transacción de guardado de factura.
     *
     * @param factura La Factura (ya persistida) que origina el débito.
     * @return El movimiento de CtaCte creado.
     */
    CtaCteMovimiento registrarDebePorFactura(Factura factura);

    // TODO: Métodos futuros
    // void registrarHaberPorPago(Pago pago);
    // BigDecimal calcularSaldoSocio(Long socioId);
    // List<CtaCteMovimiento> getResumenDeCuenta(Long socioId, LocalDate desde, LocalDate hasta);
}
