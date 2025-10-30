package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.Lectura;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz (API Interna) para la lógica de negocio de Lecturas.
 * CORREGIDO: Define la API completa que 'FacturaServiceImpl' espera.
 */
public interface LecturaService {

    // --- Métodos para Fase 3 (Spring Batch) ---
    List<Lectura> getLecturasParaFacturar(LocalDate periodo);
    void marcarLecturasComoFacturadas(List<Lectura> lecturas);

    // --- Métodos para Carga Manual (Controller) ---
    Lectura registrarLectura(Long contratoId, BigDecimal estadoActual, LocalDate fechaToma, LocalDate periodo, String tipoLectura)
            throws IllegalArgumentException;

    // --- MÉTODOS CORREGIDOS QUE 'FacturaServiceImpl' NECESITA ---

    /**
     * Busca la última lectura (por período) ANTES de la fecha de período dada.
     */
    Optional<Lectura> buscarUltimaLecturaAntesDe(Long contratoId, LocalDate fechaAntesDe);

    /**
     * Busca la primera lectura (por período) EN O DESPUÉS de la fecha de período dada.
     */
    Optional<Lectura> buscarPrimeraLecturaDesde(Long contratoId, LocalDate fechaDesde);
}

