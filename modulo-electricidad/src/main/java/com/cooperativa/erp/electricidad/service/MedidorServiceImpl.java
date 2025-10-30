package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.core.entity.Suministro; // Import desde core
import com.cooperativa.erp.core.repository.SuministroRepository; // Import desde core
import com.cooperativa.erp.electricidad.entity.Medidor;
import com.cooperativa.erp.electricidad.repository.MedidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para operaciones de escritura

import java.util.Optional;

@Service // Marca esta clase como un Bean de Servicio de Spring
public class MedidorServiceImpl implements MedidorService {

    private final MedidorRepository medidorRepository;
    private final SuministroRepository suministroRepository; // Necesario para asignarSuministro

    // Inyección por constructor
    public MedidorServiceImpl(MedidorRepository medidorRepository, SuministroRepository suministroRepository) {
        this.medidorRepository = medidorRepository;
        this.suministroRepository = suministroRepository;
    }

    @Override
    @Transactional(readOnly = true) // Transacción de solo lectura para búsquedas
    public Optional<Medidor> buscarPorNumero(String numero) {
        return medidorRepository.findByNumero(numero);
    }

    @Override
    @Transactional // Transacción de escritura (requiere commit/rollback)
    public Medidor crearOActualizarMedidor(Medidor medidor) throws Exception {
        // Validación básica (podría ser más robusta)
        if (medidor.getNumero() == null || medidor.getNumero().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de medidor no puede estar vacío.");
        }

        // Si es un medidor nuevo (sin ID), verificamos si el número ya existe
        if (medidor.getId() == null) {
            Optional<Medidor> existente = medidorRepository.findByNumero(medidor.getNumero());
            if (existente.isPresent()) {
                throw new Exception("Ya existe un medidor con el número: " + medidor.getNumero());
            }
        }
        // Si tiene ID, JPA se encargará de actualizar (o fallará si intentamos cambiar el número a uno duplicado)

        // Asegurar valores por defecto si son nulos (ej. constante)
        if (medidor.getConstanteMultiplicacion() == null) {
            medidor.setConstanteMultiplicacion(java.math.BigDecimal.ONE);
        }


        return medidorRepository.save(medidor);
    }

    @Override
    @Transactional // Transacción de escritura
    public Medidor asignarSuministro(Long medidorId, Long suministroId) throws Exception {
        // 1. Buscar el medidor
        Medidor medidor = medidorRepository.findById(medidorId)
                .orElseThrow(() -> new Exception("Medidor no encontrado con ID: " + medidorId));

        // 2. Verificar si ya está asociado a OTRO suministro (o al mismo)
        if (medidor.getSuministro() != null) {
            if (medidor.getSuministro().getId().equals(suministroId)) {
                return medidor; // Ya está asociado a este suministro, no hacemos nada
            } else {
                throw new Exception("El medidor " + medidor.getNumero() + " ya está asociado a otro suministro (ID: " + medidor.getSuministro().getId() + ")");
            }
        }

        // 3. Buscar el suministro
        Suministro suministro = suministroRepository.findById(suministroId)
                .orElseThrow(() -> new Exception("Suministro no encontrado con ID: " + suministroId));

        // 4. Realizar la asignación
        medidor.setSuministro(suministro);

        // 5. Guardar el medidor actualizado
        return medidorRepository.save(medidor);
    }
}

