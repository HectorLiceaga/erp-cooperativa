package com.cooperativa.erp.contabilidad.repository;

import com.cooperativa.erp.contabilidad.entity.ParametroContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametroContableRepository extends JpaRepository<ParametroContable, Long> {

    /**
     * Busca un parámetro por su clave única de negocio.
     * @param clave Ej: "CTA_VENTA_ENERGIA"
     * @return El parámetro contable
     */
    Optional<ParametroContable> findByClave(String clave);
}
