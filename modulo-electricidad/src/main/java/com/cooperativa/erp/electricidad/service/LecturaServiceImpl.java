package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.Lectura;
import com.cooperativa.erp.electricidad.entity.Medidor;
import com.cooperativa.erp.electricidad.repository.LecturaRepository;
import com.cooperativa.erp.electricidad.repository.MedidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LecturaServiceImpl implements LecturaService {

    private final LecturaRepository lecturaRepository;
    private final MedidorRepository medidorRepository; // Necesitamos el medidor para crear la lectura

    public LecturaServiceImpl(LecturaRepository lecturaRepository, MedidorRepository medidorRepository) {
        this.lecturaRepository = lecturaRepository;
        this.medidorRepository = medidorRepository;
    }

    @Override
    @Transactional // Operación de escritura y lectura (para buscar la última)
    public Lectura registrarLectura(Long medidorId, LocalDate fechaLectura, BigDecimal estado, String tipoLectura) throws Exception {
        // 1. Validar datos de entrada básicos
        if (medidorId == null || fechaLectura == null || estado == null || estado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Datos de lectura inválidos (null o estado negativo).");
        }

        // 2. Buscar el medidor
        Medidor medidor = medidorRepository.findById(medidorId)
                .orElseThrow(() -> new Exception("Medidor no encontrado con ID: " + medidorId)); // Usaremos excepción personalizada

        // 3. Buscar la última lectura para validación
        Optional<Lectura> ultimaLecturaOpt = buscarUltimaLecturaAntesDe(medidorId, fechaLectura.plusDays(1)); // Buscamos hasta hoy inclusive

        if (ultimaLecturaOpt.isPresent()) {
            Lectura ultima = ultimaLecturaOpt.get();
            // 4a. Validar fecha posterior
            if (!fechaLectura.isAfter(ultima.getFechaLectura())) {
                throw new Exception("La fecha de la nueva lectura debe ser posterior a la última registrada (" + ultima.getFechaLectura() + ")");
            }
            // 4b. Validar estado mayor o igual (considerar vueltas de medidor en un futuro)
            if (estado.compareTo(ultima.getEstado()) < 0) {
                // Por ahora lanzamos error, luego podríamos manejar 'vueltas'
                throw new Exception("El estado de la nueva lectura (" + estado + ") no puede ser menor al último estado registrado (" + ultima.getEstado() + ")");
            }
        }

        // 5. Crear y guardar la nueva lectura
        Lectura nuevaLectura = new Lectura(medidor, fechaLectura, estado, tipoLectura);
        // Calcular consumo si hay lectura anterior (podría ir en otro método/servicio)
        if(ultimaLecturaOpt.isPresent()) {
            BigDecimal consumo = estado.subtract(ultimaLecturaOpt.get().getEstado());
            // Podríamos necesitar multiplicar por la constante del medidor aquí
            // BigDecimal consumoReal = consumo.multiply(medidor.getConstante());
            // nuevaLectura.setConsumoCalculado(consumoReal); // Si agregamos campo consumo
        }

        return lecturaRepository.save(nuevaLectura);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lectura> buscarUltimaLecturaAntesDe(Long medidorId, LocalDate fechaMaxima) {
        return lecturaRepository.findTopByMedidorIdAndFechaLecturaBeforeOrderByFechaLecturaDesc(medidorId, fechaMaxima);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lectura> buscarLecturasPorRango(Long medidorId, LocalDate fechaDesde, LocalDate fechaHasta) {
        return lecturaRepository.findByMedidorIdAndFechaLecturaBetweenOrderByFechaLecturaAsc(medidorId, fechaDesde, fechaHasta);
    }
}

