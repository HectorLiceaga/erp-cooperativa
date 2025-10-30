package com.cooperativa.erp.contabilidad.service;

import com.cooperativa.erp.contabilidad.dto.AsientoDTO;
import com.cooperativa.erp.contabilidad.entity.Asiento;
import com.cooperativa.erp.contabilidad.entity.AsientoDetalle;
import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;
import com.cooperativa.erp.contabilidad.repository.AsientoRepository;
import com.cooperativa.erp.contabilidad.repository.PlanDeCuentasRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Transactional(readOnly = true)
    public List<AsientoDTO> buscarAsientosPorFechas(LocalDate fechaDesde, LocalDate fechaHasta) {
        return asientoRepository.findByFechaBetween(fechaDesde, fechaHasta).stream()
                .map(AsientoDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsientoDTO> getAsientoById(Long id) {
        return asientoRepository.findById(id).map(AsientoDTO::new);
    }

    @Override
    @Transactional
    public AsientoDTO registrarAsientoManual(AsientoDTO asientoDTO) throws IllegalArgumentException {
        log.info("Registrando asiento manual: {}", asientoDTO.getDescripcion());

        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;

        Asiento asiento = new Asiento();
        asiento.setFecha(asientoDTO.getFecha());
        asiento.setDescripcion(asientoDTO.getDescripcion());
        asiento.setEstado("PENDIENTE"); // Los asientos manuales nacen pendientes

        for (AsientoDTO.AsientoDetalleDTO detalleDTO : asientoDTO.getDetalles()) {

            // Validación 1: Debe o Haber debe ser mayor a cero
            if (detalleDTO.getDebe().compareTo(BigDecimal.ZERO) == 0 && detalleDTO.getHaber().compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("El detalle para la cuenta " + detalleDTO.getCodigoCuenta() + " tiene Debe y Haber en cero.");
            }

            // Validación 2: No puede tener Debe y Haber en el mismo renglón
            if (detalleDTO.getDebe().compareTo(BigDecimal.ZERO) > 0 && detalleDTO.getHaber().compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException("El detalle para la cuenta " + detalleDTO.getCodigoCuenta() + " tiene importes en Debe y Haber.");
            }

            // Validación 3: La cuenta debe existir y ser imputable
            PlanDeCuentas cuenta = planDeCuentasRepository.findByCodigo(detalleDTO.getCodigoCuenta())
                    .orElseThrow(() -> new IllegalArgumentException("La cuenta contable con código " + detalleDTO.getCodigoCuenta() + " no existe."));

            if (!cuenta.getImputable()) {
                throw new IllegalArgumentException("La cuenta contable '" + cuenta.getDescripcion() + "' no es imputable.");
            }

            // Crear el detalle y sumarlo al asiento
            AsientoDetalle detalle = new AsientoDetalle(
                    asiento,
                    cuenta,
                    detalleDTO.getDebe(),
                    detalleDTO.getHaber()
            );

            asiento.addDetalle(detalle); // addDetalle actualiza los totales de la cabecera

            totalDebe = totalDebe.add(detalleDTO.getDebe());
            totalHaber = totalHaber.add(detalleDTO.getHaber());
        }

        // Validación 4: El asiento debe balancear (Debe == Haber)
        // Usamos compareTo para comparar BigDecimal
        if (totalDebe.compareTo(totalHaber) != 0) {
            log.warn("Asiento desbalanceado: Debe={} / Haber={}", totalDebe, totalHaber);
            throw new IllegalArgumentException("El asiento no balancea. Total Debe: " + totalDebe + ", Total Haber: " + totalHaber);
        }

        log.info("Asiento balanceado. Guardando...");
        Asiento asientoGuardado = asientoRepository.save(asiento);

        return new AsientoDTO(asientoGuardado);
    }
}
