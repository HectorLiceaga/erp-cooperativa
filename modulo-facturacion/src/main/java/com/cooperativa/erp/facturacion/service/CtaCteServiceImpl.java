package com.cooperativa.erp.facturacion.service;

import com.cooperativa.erp.facturacion.entity.CtaCteMovimiento;
import com.cooperativa.erp.facturacion.entity.Factura;
import com.cooperativa.erp.facturacion.repository.CtaCteMovimientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CtaCteServiceImpl implements CtaCteService {

    private static final Logger log = LoggerFactory.getLogger(CtaCteServiceImpl.class);

    private final CtaCteMovimientoRepository ctaCteMovimientoRepository;

    public CtaCteServiceImpl(CtaCteMovimientoRepository ctaCteMovimientoRepository) {
        this.ctaCteMovimientoRepository = ctaCteMovimientoRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY) // Exige una transacción existente (la de FacturaServiceImpl)
    public CtaCteMovimiento registrarDebePorFactura(Factura factura) {
        if (factura == null || factura.getId() == null) {
            throw new IllegalArgumentException("La factura debe estar persistida para registrar el movimiento en CtaCte.");
        }

        log.debug("Registrando movimiento DEBE en CtaCte para Factura ID: {}", factura.getId());

        // Usamos el constructor helper de la entidad
        CtaCteMovimiento movimiento = new CtaCteMovimiento(factura);

        CtaCteMovimiento guardado = ctaCteMovimientoRepository.save(movimiento);

        log.info("Movimiento CtaCte ID: {} (DEBE de ${}) registrado para Factura ID: {}",
                guardado.getId(), guardado.getImporte(), factura.getId());

        return guardado;
    }
}
