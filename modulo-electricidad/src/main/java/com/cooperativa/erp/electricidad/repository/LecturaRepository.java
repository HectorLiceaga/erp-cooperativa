package com.cooperativa.erp.electricidad.repository;

import com.cooperativa.erp.electricidad.entity.Lectura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LecturaRepository extends JpaRepository<Lectura, Long> {

    // Buscar la última lectura de un medidor para obtener el estado anterior
    Optional<Lectura> findTopByMedidorIdOrderByPeriodoDesc(Long medidorId);

    // Buscar lecturas de un medidor en un período específico (puede haber más de una si hay reclamos)
    List<Lectura> findByMedidorIdAndPeriodo(Long medidorId, LocalDate periodo);
}

