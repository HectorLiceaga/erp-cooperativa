package com.cooperativa.erp.electricidad.repository;

import com.cooperativa.erp.electricidad.entity.PrecioConcepto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrecioConceptoRepository extends JpaRepository<PrecioConcepto, Long> {

    // Método clave: Buscar los precios vigentes para una categoría tarifaria en una fecha dada
    @Query("SELECT pc FROM PrecioConcepto pc " +
            "WHERE pc.categoriaTarifaria.id = :categoriaId " +
            "AND pc.fechaDesde <= :fecha " +
            "AND (pc.fechaHasta IS NULL OR pc.fechaHasta > :fecha)")
    List<PrecioConcepto> findPreciosVigentes(
            @Param("categoriaId") Long categoriaId,
            @Param("fecha") LocalDate fecha
    );

    // Podríamos necesitar buscar un concepto específico
    @Query("SELECT pc FROM PrecioConcepto pc " +
            "WHERE pc.categoriaTarifaria.id = :categoriaId " +
            "AND pc.conceptoFacturable.id = :conceptoId " +
            "AND pc.fechaDesde <= :fecha " +
            "AND (pc.fechaHasta IS NULL OR pc.fechaHasta > :fecha)")
    List<PrecioConcepto> findPrecioConceptoVigente(
            @Param("categoriaId") Long categoriaId,
            @Param("conceptoId") Long conceptoId,
            @Param("fecha") LocalDate fecha
    );

}

