package com.cooperativa.erp.contabilidad.service;

import com.cooperativa.erp.contabilidad.dto.AsientoDTO;
import com.cooperativa.erp.contabilidad.entity.Asiento;
import com.cooperativa.erp.contabilidad.entity.AsientoDetalle;
import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;
import com.cooperativa.erp.contabilidad.repository.AsientoRepository;
import com.cooperativa.erp.contabilidad.repository.PlanDeCuentasRepository;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections; // Importar Collections
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Importar Collectors

@Service
public class AsientoServiceImpl implements AsientoService {

    private static final Logger log = LoggerFactory.getLogger(AsientoServiceImpl.class);

    private final AsientoRepository asientoRepository;
    private final PlanDeCuentasRepository planDeCuentasRepository;

    public AsientoServiceImpl(AsientoRepository asientoRepository, PlanDeCuentasRepository planDeCuentasRepository) {
        this.asientoRepository = asientoRepository;
        this.planDeCuentasRepository = planDeCuentasRepository;
    }

    @Override
    @Transactional
    public AsientoDTO registrarAsientoManual(AsientoDTO dto) throws IllegalArgumentException, ValidationException {
        log.info("Registrando nuevo asiento manual para fecha {}", dto.getFecha());

        BigDecimal totalDebeDTO = dto.getDetalles().stream()
                .map(AsientoDTO.AsientoDetalleDTO::getDebe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalHaberDTO = dto.getDetalles().stream()
                .map(AsientoDTO.AsientoDetalleDTO::getHaber)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebeDTO.compareTo(totalHaberDTO) != 0) {
            log.warn("Validación fallida: Asiento desbalanceado. Debe: {}, Haber: {}", totalDebeDTO, totalHaberDTO);
            throw new ValidationException("Asiento desbalanceado. Debe: " + totalDebeDTO + ", Haber: " + totalHaberDTO);
        }
        log.debug("Validación de balance OK (Debe=Haber={})", totalDebeDTO);

        Asiento asiento = new Asiento();
        asiento.setFecha(dto.getFecha());
        asiento.setDescripcion(dto.getDescripcion());
        asiento.setOrigen(dto.getOrigen() != null ? dto.getOrigen() : "carga_manual");
        asiento.setEstado("CONFIRMADO");
        asiento.setTotalDebe(totalDebeDTO);
        asiento.setTotalHaber(totalHaberDTO);

        for (AsientoDTO.AsientoDetalleDTO detalleDTO : dto.getDetalles()) {
            if (detalleDTO.getDebe().compareTo(BigDecimal.ZERO) == 0 && detalleDTO.getHaber().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            PlanDeCuentas cuenta = planDeCuentasRepository.findByCodigo(detalleDTO.getCodigoCuenta())
                    .orElseThrow(() -> new IllegalArgumentException("El código de cuenta " + detalleDTO.getCodigoCuenta() + " no existe."));

            if (!cuenta.getImputable()) {
                log.warn("Validación fallida: La cuenta {} ({}) no es imputable.", cuenta.getCodigo(), cuenta.getNombre());
                throw new ValidationException("La cuenta " + cuenta.getCodigo() + " (" + cuenta.getNombre() + ") no es imputable.");
            }
            log.debug("Cuenta {} validada como imputable.", cuenta.getCodigo());

            AsientoDetalle detalle = new AsientoDetalle(
                    asiento,
                    cuenta,
                    detalleDTO.getDescripcion(),
                    detalleDTO.getDebe(),
                    detalleDTO.getHaber()
            );
            asiento.addDetalle(detalle);
        }

        Asiento asientoGuardado = asientoRepository.save(asiento);
        log.info("Asiento ID {} guardado exitosamente.", asientoGuardado.getId());

        return new AsientoDTO(asientoGuardado);
    }

    /**
     * ¡MÉTODO AÑADIDO QUE FALTABA!
     * Busca asientos en un rango de fechas.
     * @param fechaDesde Fecha de inicio.
     * @param fechaHasta Fecha de fin.
     * @return Lista de DTOs de asientos.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AsientoDTO> buscarAsientosPorFechas(LocalDate fechaDesde, LocalDate fechaHasta) {
        log.debug("Buscando asientos entre {} y {}", fechaDesde, fechaHasta);

        // TODO: Descomentar y crear el método en AsientoRepository cuando esté listo.
        // List<Asiento> asientos = asientoRepository.findByFechaBetween(fechaDesde, fechaHasta);
        // return asientos.stream().map(AsientoDTO::new).collect(Collectors.toList());

        // Devolvemos vacío para que compile
        return Collections.emptyList();
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<AsientoDTO> getAsientoById(Long id) {
        log.debug("Buscando Asiento ID: {}", id);
        return asientoRepository.findById(id).map(AsientoDTO::new);
    }
}