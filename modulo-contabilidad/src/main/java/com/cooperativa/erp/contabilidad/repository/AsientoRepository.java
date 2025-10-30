package com.cooperativa.erp.contabilidad.repository;

import com.cooperativa.erp.contabilidad.entity.Asiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long> {

    List<Asiento> findByFechaBetween(LocalDate fechaDesde, LocalDate fechaHasta);
}
