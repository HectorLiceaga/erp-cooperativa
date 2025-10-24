package com.cooperativa.erp.electricidad.repository;

import com.cooperativa.erp.electricidad.entity.Lectura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // Necesario para Optional

@Repository
public interface LecturaRepository extends JpaRepository<Lectura, Long> {

    // --- NUEVO MÉTODO ---
    /**
     * Busca la lectura más reciente para un medidor específico antes de una fecha dada.
     * Spring Data JPA infiere la consulta: "SELECT l FROM Lectura l WHERE l.medidor.id = ?1 AND l.fechaLectura < ?2 ORDER BY l.fechaLectura DESC LIMIT 1"
     * @param medidorId ID del medidor.
     * @param fechaMaxima Fecha límite (exclusiva).
     * @return Un Optional con la lectura encontrada, o vacío.
     */
    Optional<Lectura> findTopByMedidorIdAndFechaLecturaBeforeOrderByFechaLecturaDesc(Long medidorId, LocalDate fechaMaxima);
    // --- FIN NUEVO MÉTODO ---

    // --- NUEVO MÉTODO ---
    /**
     * Busca todas las lecturas para un medidor específico dentro de un rango de fechas, ordenadas ascendentemente por fecha.
     * Spring Data JPA infiere la consulta: "SELECT l FROM Lectura l WHERE l.medidor.id = ?1 AND l.fechaLectura BETWEEN ?2 AND ?3 ORDER BY l.fechaLectura ASC"
     * @param medidorId ID del medidor.
     * @param fechaDesde Fecha de inicio (inclusiva).
     * @param fechaHasta Fecha de fin (inclusiva).
     * @return Lista de lecturas encontradas.
     */
    List<Lectura> findByMedidorIdAndFechaLecturaBetweenOrderByFechaLecturaAsc(Long medidorId, LocalDate fechaDesde, LocalDate fechaHasta);
    // --- FIN NUEVO MÉTODO ---

}

