package com.cooperativa.erp.electricidad.repository;

import com.cooperativa.erp.core.entity.ContratoServicio;
import com.cooperativa.erp.core.entity.Suministro;
import com.cooperativa.erp.electricidad.entity.ContratoElectricidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContratoElectricidadRepository extends JpaRepository<ContratoElectricidad, Long> {

    /**
     * Busca el contrato de electricidad activo para un suministro específico.
     * Asumimos que solo puede haber UN contrato de electricidad ACTIVO por suministro.
     */
    @Query("SELECT c FROM ContratoElectricidad c " +
            "WHERE c.suministro = :suministro " +
            "AND c.estado = :estado")
    Optional<ContratoElectricidad> findBySuministroAndEstado(Suministro suministro, ContratoServicio.EstadoContrato estado);

}
