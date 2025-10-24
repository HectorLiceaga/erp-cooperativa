package com.cooperativa.erp.electricidad.repository;

import com.cooperativa.erp.electricidad.entity.Lectura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LecturaRepository extends JpaRepository<Lectura, Long> {

    /**
     * Busca la última lectura registrada para un medidor ANTES de una fecha dada.
     * Usado para encontrar la lectura inmediatamente anterior a una nueva.
     */
    Optional<Lectura> findFirstByMedidorIdAndFechaLecturaBeforeOrderByFechaLecturaDesc(Long medidorId, LocalDate fecha);

    /**
     * Busca todas las lecturas para un medidor dentro de un rango de fechas.
     */
    List<Lectura> findByMedidorIdAndFechaLecturaBetweenOrderByFechaLecturaAsc(Long medidorId, LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Busca la última lectura registrada para un medidor, independientemente de la fecha.
     * Spring Data JPA infiere: SELECT * FROM elec_lecturas WHERE medidor_id = ? ORDER BY fecha_lectura DESC LIMIT 1
     * @param medidorId ID del medidor
     * @return Un Optional con la última lectura si existe.
     */
    Optional<Lectura> findTopByMedidorIdOrderByFechaLecturaDesc(Long medidorId);

}


