package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.PrecioConcepto;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz para gestionar la lógica de negocio relacionada con los precios de conceptos tarifarios.
 */
public interface PrecioConceptoService {

    /**
     * Busca todos los precios de conceptos vigentes para una categoría tarifaria específica
     * en una fecha determinada. Esto incluye manejar la vigencia (fechaDesde/fechaHasta).
     *
     * @param categoriaId El ID de la CategoriaTarifaria.
     * @param fecha La fecha para la cual se quieren los precios vigentes.
     * @return Una lista de PrecioConcepto vigentes en esa fecha para esa categoría.
     * Puede devolver una lista vacía si no se encuentran precios.
     */
    List<PrecioConcepto> obtenerPreciosVigentes(Long categoriaId, LocalDate fecha);

    // --- Podríamos agregar más métodos aquí a futuro ---
    // Ej: obtenerPrecioEspecifico(Long categoriaId, Long conceptoId, LocalDate fecha, BigDecimal consumo)
    // para manejar escalones de consumo.
}

