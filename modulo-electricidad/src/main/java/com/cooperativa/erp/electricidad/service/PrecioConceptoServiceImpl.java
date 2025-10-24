package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.PrecioConcepto;
import com.cooperativa.erp.electricidad.repository.PrecioConceptoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para servicios que leen datos

import java.time.LocalDate;
import java.util.List;

@Service // Marca esta clase como un Bean de Servicio de Spring
public class PrecioConceptoServiceImpl implements PrecioConceptoService {

    private final PrecioConceptoRepository precioConceptoRepository;

    // Inyección de dependencias por constructor (mejor práctica)
    public PrecioConceptoServiceImpl(PrecioConceptoRepository precioConceptoRepository) {
        this.precioConceptoRepository = precioConceptoRepository;
    }

    @Override
    @Transactional(readOnly = true) // Indica que este método solo lee datos (optimización)
    public List<PrecioConcepto> obtenerPreciosVigentes(Long categoriaId, LocalDate fecha) {
        // Validaciones básicas (podrían moverse a un validador o ser más robustas)
        if (categoriaId == null || fecha == null) {
            // Podríamos lanzar una excepción personalizada aquí (ej. InvalidArgumentException)
            // Por ahora, devolvemos lista vacía o podríamos lanzar IllegalArgumentException
            throw new IllegalArgumentException("El ID de categoría y la fecha no pueden ser nulos.");
        }
        // Delegamos la lógica de búsqueda al repositorio usando el método con @Query que creamos
        return precioConceptoRepository.findPreciosVigentes(categoriaId, fecha);
    }

    // --- Implementaciones de futuros métodos irían aquí ---

}

