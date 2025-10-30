package com.cooperativa.erp.contabilidad.service;

import com.cooperativa.erp.contabilidad.dto.AsientoDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la lógica de negocio de Asientos Contables.
 */
public interface AsientoService {

    /**
     * Registra un nuevo asiento contable manual.
     * Valida que el asiento balancee (Debe == Haber).
     * @param asientoDTO El DTO con la cabecera y los detalles.
     * @return El DTO del asiento guardado (con IDs).
     * @throws IllegalArgumentException si el asiento no balancea o una cuenta no es imputable.
     */
    AsientoDTO registrarAsientoManual(AsientoDTO asientoDTO) throws IllegalArgumentException;

    /**
     * Busca asientos en un rango de fechas.
     * @param fechaDesde Fecha de inicio.
     * @param fechaHasta Fecha de fin.
     * @return Lista de DTOs de asientos.
     */
    List<AsientoDTO> buscarAsientosPorFechas(LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Busca un asiento por su ID.
     * @param id El ID del asiento.
     * @return El DTO del asiento (con detalles).
     */
    Optional<AsientoDTO> getAsientoById(Long id);

    // TODO: Faltarían métodos para "confirmar" o "anular" asientos,
    // pero para el ABM inicial con esto es suficiente.
}
