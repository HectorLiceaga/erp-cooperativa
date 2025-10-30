package com.cooperativa.erp.contabilidad.repository;

import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanDeCuentasRepository extends JpaRepository<PlanDeCuentas, Long> {

    Optional<PlanDeCuentas> findByCodigo(String codigo);

    List<PlanDeCuentas> findByPadreId(Long padreId);

    List<PlanDeCuentas> findByImputable(boolean imputable);
}
