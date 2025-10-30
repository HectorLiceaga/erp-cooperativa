package com.cooperativa.erp.core.repository;

import com.cooperativa.erp.core.entity.ContratoServicio;
import com.cooperativa.erp.core.entity.Suministro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoServicioRepository extends JpaRepository<ContratoServicio, Long> {

    /**
     * Busca todos los contratos (de cualquier tipo) asociados a un suministro.
     */
    List<ContratoServicio> findBySuministro(Suministro suministro);

    /**
     * Busca todos los contratos activos de un suministro.
     */
    List<ContratoServicio> findBySuministroAndEstado(Suministro suministro, ContratoServicio.EstadoContrato estado);
}
