package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.Medidor;
import java.util.Optional;

/**
 * Interfaz para la lógica de negocio relacionada con Medidores.
 */
public interface MedidorService {

    /**
     * Crea o actualiza un medidor.
     * @param medidor El medidor a guardar.
     * @return El medidor guardado (con ID asignado si es nuevo).
     */
    Medidor guardar(Medidor medidor);

    /**
     * Busca un medidor por su ID.
     * @param id El ID del medidor.
     * @return Un Optional conteniendo el medidor si se encuentra, o vacío si no.
     */
    Optional<Medidor> buscarPorId(Long id);

    /**
     * Busca un medidor por su número/serie.
     * @param numero El número/serie del medidor.
     * @return Un Optional conteniendo el medidor si se encuentra, o vacío si no.
     */
    Optional<Medidor> buscarPorNumero(String numero);

    // --- Podríamos agregar métodos para asociar/desasociar a Suministro ---
    // Medidor asociarASuministro(Long medidorId, Long suministroId);
    // Medidor desasociarDeSuministro(Long medidorId);
}

