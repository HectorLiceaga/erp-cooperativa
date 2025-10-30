package com.cooperativa.erp.electricidad.repository;

import com.cooperativa.erp.electricidad.entity.Lectura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Lectura.
 * CORREGIDO: Incluye los métodos que FacturaServiceImpl necesita.
 */
@Repository
public interface LecturaRepository extends JpaRepository<Lectura, Long> {

    // --- Métodos para Carga/Validación (del ServiceImpl) ---

    /**
     * Busca la última lectura registrada (por período) para un contrato.
     */
    Optional<Lectura> findTopByContratoElectricidadIdOrderByFechaPeriodoDesc(Long contratoId);

    // --- Métodos para Spring Batch (del ServiceImpl) ---

    /**
     * Busca todas las lecturas de un período que aún no han sido facturadas.
     */
    @Query("SELECT l FROM Lectura l WHERE l.fechaPeriodo = :periodo AND l.facturada = false")
    List<Lectura> findLecturasPendientesDeFacturacion(LocalDate periodo);


    // --- MÉTODOS AÑADIDOS QUE 'FacturaServiceImpl' NECESITA ---

    /**
     * Busca la última lectura (ordenada por período) registrada ANTES de una fecha de período dada.
     */
    Optional<Lectura> findTopByContratoElectricidadIdAndFechaPeriodoBeforeOrderByFechaPeriodoDesc(Long contratoId, LocalDate fechaAntesDe);

    /**
     * Busca la primera lectura (ordenada por período) registrada EN O DESPUÉS de una fecha de período dada.
     */
    Optional<Lectura> findTopByContratoElectricidadIdAndFechaPeriodoAfterOrderByFechaPeriodoAsc(Long contratoId, LocalDate fechaDesde);

}

