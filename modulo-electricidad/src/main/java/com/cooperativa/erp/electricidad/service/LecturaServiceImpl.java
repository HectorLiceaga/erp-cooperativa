package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.Lectura;
import com.cooperativa.erp.electricidad.entity.Medidor;
import com.cooperativa.erp.electricidad.repository.LecturaRepository;
import com.cooperativa.erp.electricidad.repository.MedidorRepository;
import org.slf4j.Logger; // Importar Logger
import org.slf4j.LoggerFactory; // Importar LoggerFactory
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LecturaServiceImpl implements LecturaService {

    // Crear una instancia del Logger
    private static final Logger log = LoggerFactory.getLogger(LecturaServiceImpl.class);

    private final LecturaRepository lecturaRepository;
    private final MedidorRepository medidorRepository;

    public LecturaServiceImpl(LecturaRepository lecturaRepository, MedidorRepository medidorRepository) {
        this.lecturaRepository = lecturaRepository;
        this.medidorRepository = medidorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lectura> buscarUltimaLecturaAntesDe(Long medidorId, LocalDate fecha) {
        log.debug("Buscando última lectura para medidor {} antes de {}", medidorId, fecha);
        return lecturaRepository.findFirstByMedidorIdAndFechaLecturaBeforeOrderByFechaLecturaDesc(medidorId, fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lectura> buscarLecturasPorRango(Long medidorId, LocalDate fechaDesde, LocalDate fechaHasta) {
        log.debug("Buscando lecturas para medidor {} entre {} y {}", medidorId, fechaDesde, fechaHasta);
        return lecturaRepository.findByMedidorIdAndFechaLecturaBetweenOrderByFechaLecturaAsc(medidorId, fechaDesde, fechaHasta);
    }

    @Override
    @Transactional
    public Lectura registrarLectura(Long medidorId, LocalDate fechaLectura, BigDecimal estado, String tipoLectura) throws IllegalArgumentException {
        log.info("Intentando registrar lectura para medidor {}: Fecha={}, Estado={}, Tipo={}", medidorId, fechaLectura, estado, tipoLectura);

        // 1. Validar datos de entrada básicos
        if (medidorId == null || fechaLectura == null || estado == null || tipoLectura == null || tipoLectura.isBlank()) {
            log.warn("Validación fallida: Datos de lectura incompletos.");
            throw new IllegalArgumentException("Datos de lectura incompletos o inválidos.");
        }
        if (estado.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Validación fallida: Estado negativo ({}).", estado);
            throw new IllegalArgumentException("El estado no puede ser negativo.");
        }

        // 2. Buscar el medidor
        Medidor medidor = medidorRepository.findById(medidorId)
                .orElseThrow(() -> {
                    log.warn("Validación fallida: No se encontró medidor con ID {}.", medidorId);
                    return new IllegalArgumentException("No se encontró el medidor con ID: " + medidorId);
                });
        log.debug("Medidor encontrado: {}", medidor.getNumero());

        // 3. Buscar la ÚLTIMA lectura REAL registrada para este medidor
        log.debug("Buscando la última lectura registrada para el medidor {}", medidorId);
        Optional<Lectura> ultimaLecturaRealOpt = lecturaRepository.findTopByMedidorIdOrderByFechaLecturaDesc(medidorId);

        if (ultimaLecturaRealOpt.isPresent()) {
            Lectura ultimaReal = ultimaLecturaRealOpt.get();
            log.info("Última lectura encontrada: Fecha={}, Estado={}", ultimaReal.getFechaLectura(), ultimaReal.getEstado());

            // 4a. Validar que la nueva fecha sea estrictamente posterior a la ÚLTIMA REAL
            log.debug("Validando fecha: {} isAfter {}", fechaLectura, ultimaReal.getFechaLectura());
            if (!fechaLectura.isAfter(ultimaReal.getFechaLectura())) {
                log.warn("Validación de fecha fallida: {} no es posterior a {}", fechaLectura, ultimaReal.getFechaLectura());
                throw new IllegalArgumentException("La fecha de la nueva lectura ("+ fechaLectura + ") debe ser estrictamente posterior a la última registrada (" + ultimaReal.getFechaLectura() + ")");
            }

            // 4b. Validar estado mayor o igual (respecto a la ÚLTIMA REAL)
            log.debug("Validando estado: {} compareTo {} >= 0 ?", estado, ultimaReal.getEstado());
            if (estado.compareTo(ultimaReal.getEstado()) < 0) {
                log.warn("Validación de estado fallida: {} es menor que {}", estado, ultimaReal.getEstado());
                throw new IllegalArgumentException("El estado de la nueva lectura (" + estado + ") no puede ser menor al último estado registrado (" + ultimaReal.getEstado() + ")");
            }
            log.debug("Validaciones de secuencia superadas.");

        } else {
            log.info("No se encontraron lecturas anteriores para el medidor {}. Es la primera lectura.", medidorId);
            // Si no hay ninguna lectura anterior, es la primera. No hay validaciones de secuencia.
        }

        // 5. Crear y guardar la nueva lectura
        log.debug("Creando nueva entidad Lectura...");
        Lectura nuevaLectura = new Lectura(medidor, fechaLectura, estado, tipoLectura);
        Lectura guardada = lecturaRepository.save(nuevaLectura);
        log.info("Lectura registrada exitosamente con ID {}", guardada.getId());
        return guardada;
    }
}

