package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.ContratoElectricidad;
import com.cooperativa.erp.electricidad.entity.Lectura;
import com.cooperativa.erp.electricidad.repository.ContratoElectricidadRepository;
import com.cooperativa.erp.electricidad.repository.LecturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de Lecturas.
 * CORREGIDO: Implementa los métodos que 'FacturaServiceImpl' necesita.
 */
@Service
@Transactional(readOnly = true)
public class LecturaServiceImpl implements LecturaService {

    private static final Logger log = LoggerFactory.getLogger(LecturaServiceImpl.class);

    private final LecturaRepository lecturaRepository;
    private final ContratoElectricidadRepository contratoRepository;

    @Autowired
    public LecturaServiceImpl(LecturaRepository lecturaRepository, ContratoElectricidadRepository contratoRepository) {
        this.lecturaRepository = lecturaRepository;
        this.contratoRepository = contratoRepository;
    }

    @Override
    public List<Lectura> getLecturasParaFacturar(LocalDate periodo) {
        log.debug("Buscando lecturas pendientes para facturar del período {}", periodo);
        return lecturaRepository.findLecturasPendientesDeFacturacion(periodo);
    }

    @Override
    @Transactional
    public void marcarLecturasComoFacturadas(List<Lectura> lecturas) {
        if (lecturas == null || lecturas.isEmpty()) {
            return;
        }
        log.info("Marcando {} lecturas como facturadas", lecturas.size());
        for (Lectura l : lecturas) {
            l.setFacturada(true);
            lecturaRepository.save(l);
        }
        log.debug("Lecturas marcadas exitosamente.");
    }

    @Override
    @Transactional
    public Lectura registrarLectura(Long contratoId, BigDecimal estadoActual, LocalDate fechaToma, LocalDate periodo, String tipoLectura)
            throws IllegalArgumentException {

        log.info("Intentando registrar lectura para contrato {}: FechaToma={}, Periodo={}, Estado={}, Tipo={}",
                contratoId, fechaToma, periodo, estadoActual, tipoLectura);

        // 1. Validaciones
        if (contratoId == null || fechaToma == null || periodo == null || estadoActual == null || tipoLectura == null || tipoLectura.isBlank()) {
            log.warn("Validación fallida: Datos de lectura incompletos.");
            throw new IllegalArgumentException("Datos de lectura incompletos o inválidos.");
        }
        if (estadoActual.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Validación fallida: Estado negativo ({}).", estadoActual);
            throw new IllegalArgumentException("El estado no puede ser negativo.");
        }

        // 2. Contrato
        ContratoElectricidad contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> {
                    log.warn("Validación fallida: No se encontró contrato con ID {}.", contratoId);
                    return new IllegalArgumentException("No se encontró el contrato con ID: " + contratoId);
                });
        log.debug("Contrato encontrado: {}", contrato.getId());

        // 3. Última lectura
        Optional<Lectura> ultimaLecturaOpt = lecturaRepository.findTopByContratoElectricidadIdOrderByFechaPeriodoDesc(contratoId);
        BigDecimal estadoAnterior = BigDecimal.ZERO;

        if (ultimaLecturaOpt.isPresent()) {
            Lectura ultimaLectura = ultimaLecturaOpt.get();
            estadoAnterior = ultimaLectura.getEstadoActual();
            log.info("Última lectura encontrada: Periodo={}, Estado={}", ultimaLectura.getFechaPeriodo(), ultimaLectura.getEstadoActual());

            // 4a. Validar período
            if (!periodo.isAfter(ultimaLectura.getFechaPeriodo())) {
                log.warn("Validación de período fallida: {} no es posterior a {}", periodo, ultimaLectura.getFechaPeriodo());
                throw new IllegalArgumentException("El período de la nueva lectura ("+ periodo + ") debe ser estrictamente posterior al último registrado (" + ultimaLectura.getFechaPeriodo() + ")");
            }

            // 4b. Validar estado
            if (estadoActual.compareTo(ultimaLectura.getEstadoActual()) < 0) {
                log.warn("Validación de estado fallida: {} es menor que {}", estadoActual, ultimaLectura.getEstadoActual());
                throw new IllegalArgumentException("El estado de la nueva lectura (" + estadoActual + ") no puede ser menor al último estado registrado (" + ultimaLectura.getEstadoActual() + ")");
            }

        } else {
            log.info("No se encontraron lecturas anteriores para el contrato {}. Es la primera lectura.", contratoId);
        }

        // 5. Calcular consumo y crear
        BigDecimal consumo = estadoActual.subtract(estadoAnterior);
        log.debug("Cálculo de consumo: {} - {} = {}", estadoActual, estadoAnterior, consumo);

        Lectura nuevaLectura = new Lectura();
        nuevaLectura.setContratoElectricidad(contrato);
        nuevaLectura.setFechaToma(fechaToma);
        nuevaLectura.setFechaPeriodo(periodo);
        nuevaLectura.setEstadoAnterior(estadoAnterior);
        nuevaLectura.setEstadoActual(estadoActual);
        nuevaLectura.setConsumoKwh(consumo);
        nuevaLectura.setFacturada(false);
        nuevaLectura.setTipoLectura(tipoLectura);

        Lectura guardada = lecturaRepository.save(nuevaLectura);
        log.info("Lectura registrada exitosamente con ID {}", guardada.getId());
        return guardada;
    }

    // --- Implementación de los métodos CORREGIDOS para 'FacturaServiceImpl' ---

    @Override
    public Optional<Lectura> buscarUltimaLecturaAntesDe(Long contratoId, LocalDate fechaAntesDe) {
        log.debug("Buscando última lectura (por período) para contrato {} ANTES DE {}", contratoId, fechaAntesDe);
        return lecturaRepository.findTopByContratoElectricidadIdAndFechaPeriodoBeforeOrderByFechaPeriodoDesc(contratoId, fechaAntesDe);
    }

    @Override
    public Optional<Lectura> buscarPrimeraLecturaDesde(Long contratoId, LocalDate fechaDesde) {
        log.debug("Buscando primera lectura (por período) para contrato {} DESDE {}", contratoId, fechaDesde);
        // NOTA: 'fechaDesde' aquí corresponde al 'fechaHasta' del período de facturación
        return lecturaRepository.findTopByContratoElectricidadIdAndFechaPeriodoAfterOrderByFechaPeriodoAsc(contratoId, fechaDesde);
    }
}

