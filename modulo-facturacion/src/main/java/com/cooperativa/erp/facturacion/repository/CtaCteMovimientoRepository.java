package com.cooperativa.erp.facturacion.repository;

import com.cooperativa.erp.facturacion.entity.CtaCteMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CtaCteMovimientoRepository extends JpaRepository<CtaCteMovimiento, Long> {

    // Aquí podríamos añadir métodos para calcular saldos de socios,
    // buscar movimientos por rango de fechas, etc.
    // Por ejemplo:
    // @Query("SELECT SUM(CASE WHEN m.tipoMovimiento = 'DEBE' THEN m.importe ELSE -m.importe END) " +
    //        "FROM CtaCteMovimiento m WHERE m.socio.id = :socioId")
    // Optional<BigDecimal> getSaldoBySocioId(Long socioId);
}
