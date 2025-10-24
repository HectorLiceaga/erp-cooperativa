package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.Medidor;
import com.cooperativa.erp.electricidad.repository.MedidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MedidorServiceImpl implements MedidorService {

    private final MedidorRepository medidorRepository;

    public MedidorServiceImpl(MedidorRepository medidorRepository) {
        this.medidorRepository = medidorRepository;
    }

    @Override
    @Transactional // Escritura
    public Medidor guardar(Medidor medidor) {
        // Aquí podríamos agregar validaciones antes de guardar
        // Ej: Verificar si el número de medidor ya existe si es uno nuevo
        return medidorRepository.save(medidor);
    }

    @Override
    @Transactional(readOnly = true) // Lectura
    public Optional<Medidor> buscarPorId(Long id) {
        return medidorRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true) // Lectura
    public Optional<Medidor> buscarPorNumero(String numero) {
        return medidorRepository.findByNumero(numero);
    }

    // --- Implementaciones futuras ---
}

