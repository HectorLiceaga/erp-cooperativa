package com.cooperativa.erp.electricidad.service;

import com.cooperativa.erp.electricidad.entity.Medidor;

import java.util.Optional;

public interface MedidorService {

    /**
     * Busca un medidor por su número/serie único.
     * @param numero El número a buscar.
     * @return Optional con el medidor si existe.
     */
    Optional<Medidor> buscarPorNumero(String numero);

    // --- MÉTODO AÑADIDO ---
    /**
     * Crea un nuevo medidor o actualiza uno existente si ya tiene ID.
     * @param medidor La entidad Medidor a guardar.
     * @return El medidor guardado (con ID asignado si era nuevo).
     * @throws Exception si ocurre un error (ej. número duplicado).
     */
    Medidor crearOActualizarMedidor(Medidor medidor) throws Exception;
    // --- FIN MÉTODO AÑADIDO ---


    // --- MÉTODO AÑADIDO ---
    /**
     * Asigna un medidor existente a un suministro existente.
     * @param medidorId El ID del medidor.
     * @param suministroId El ID del suministro.
     * @return El medidor actualizado con la asociación.
     * @throws Exception si el medidor o suministro no existen, o si el medidor ya está asociado.
     */
    Medidor asignarSuministro(Long medidorId, Long suministroId) throws Exception;
    // --- FIN MÉTODO AÑADIDO ---


    // Podríamos añadir más métodos a futuro:
    // Medidor desasignarSuministro(Long medidorId) throws Exception;
    // Medidor cambiarEstado(Long medidorId, String nuevoEstado) throws Exception;
}

