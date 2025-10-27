package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.Lectura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para el servicio de gestión de Lecturas.
 */
public interface LecturaService {

    /**
     * Busca la última lectura registrada para un medidor antes de una fecha dada.
     * @param medidorId ID del medidor.
     * @param fecha Fecha límite (exclusiva).
     * @return Optional con la última lectura si existe, Optional vacío si no.
     */
    Optional<Lectura> buscarUltimaLecturaAntesDe(Long medidorId, LocalDate fecha);

    /**
     * Busca todas las lecturas registradas para un medidor dentro de un rango de fechas.
     * @param medidorId ID del medidor.
     * @param fechaDesde Fecha de inicio del rango (inclusiva).
     * @param fechaHasta Fecha de fin del rango (inclusiva).
     * @return Lista de lecturas encontradas, ordenada por fecha ascendente.
     */
    List<Lectura> buscarLecturasPorRango(Long medidorId, LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Registra una nueva lectura para un medidor, aplicando validaciones de secuencia.
     * @param medidorId ID del medidor.
     * @param fechaLectura Fecha de la nueva lectura.
     * @param estado Estado (valor) de la nueva lectura.
     * @param tipoLectura Tipo de lectura (NORMAL, RETIRO, etc.).
     * @return La entidad Lectura guardada.
     * @throws IllegalArgumentException Si los datos son inválidos, el medidor no existe,
     * o si la fecha o estado violan la secuencia.
     */
    Lectura registrarLectura(Long medidorId, LocalDate fechaLectura, BigDecimal estado, String tipoLectura)
            throws IllegalArgumentException; // <-- Asegúrate que sea IllegalArgumentException

}

