package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.Lectura;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para la lógica de negocio relacionada con Lecturas de medidores.
 */
public interface LecturaService {

    /**
     * Registra una nueva lectura para un medidor.
     * Debería realizar validaciones (ej. fecha posterior a la última, estado mayor o igual).
     * @param medidorId El ID del medidor.
     * @param fechaLectura La fecha en que se tomó la lectura.
     * @param estado El estado (valor numérico) leído del medidor.
     * @param tipoLectura Indica si fue una lectura normal, estimada, de retiro, etc.
     * @return La entidad Lectura guardada.
     * @throws Exception Si la validación falla (ej. fecha inválida, estado inválido).
     */
    Lectura registrarLectura(Long medidorId, LocalDate fechaLectura, BigDecimal estado, String tipoLectura) throws Exception; // Usaremos excepciones personalizadas luego

    /**
     * Busca la última lectura registrada para un medidor antes de una fecha dada.
     * @param medidorId El ID del medidor.
     * @param fechaMaxima La fecha límite (exclusiva) para buscar la última lectura.
     * @return Un Optional con la última lectura si existe, o vacío si no.
     */
    Optional<Lectura> buscarUltimaLecturaAntesDe(Long medidorId, LocalDate fechaMaxima);

    /**
     * Busca todas las lecturas de un medidor en un rango de fechas.
     * @param medidorId El ID del medidor.
     * @param fechaDesde Fecha de inicio (inclusiva).
     * @param fechaHasta Fecha de fin (inclusiva).
     * @return Lista de lecturas en el rango.
     */
    List<Lectura> buscarLecturasPorRango(Long medidorId, LocalDate fechaDesde, LocalDate fechaHasta);
}

