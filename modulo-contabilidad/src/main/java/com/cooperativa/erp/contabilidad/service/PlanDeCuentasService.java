package com.cooperativa.erp.contabilidad.service;

import com.cooperativa.erp.contabilidad.dto.PlanDeCuentasDTO;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la lógica de negocio del Plan de Cuentas.
 */
public interface PlanDeCuentasService {

    /**
     * Obtiene el árbol de cuentas completo desde la raíz.
     * @return Lista de cuentas de primer nivel (padres nulos) con sus hijos anidados.
     */
    List<PlanDeCuentasDTO> getArbolDeCuentasCompleto();

    /**
     * Obtiene una cuenta específica por su ID, con sus hijos.
     * @param id El ID de la cuenta.
     * @return Un DTO de la cuenta con su sub-árbol.
     */
    Optional<PlanDeCuentasDTO> getCuentaById(Long id);

    /**
     * Obtiene una cuenta por su código de imputación.
     * @param codigo Ej: "1.01.01.001"
     * @return El DTO de la cuenta.
     */
    Optional<PlanDeCuentasDTO> getCuentaByCodigo(String codigo);

    /**
     * Crea una nueva cuenta contable.
     * @param cuentaDTO DTO con los datos de la nueva cuenta.
     * @return El DTO de la cuenta creada (con su ID).
     * @throws IllegalArgumentException si el 'padreId' no es válido o si el código ya existe.
     */
    PlanDeCuentasDTO crearCuenta(PlanDeCuentasDTO cuentaDTO) throws IllegalArgumentException;

    /**
     * Actualiza una cuenta existente.
     * @param id El ID de la cuenta a actualizar.
     * @param cuentaDTO DTO con los nuevos datos.
     * @return El DTO de la cuenta actualizada.
     * @throws IllegalArgumentException si la cuenta no existe o los datos son inválidos.
     */
    PlanDeCuentasDTO actualizarCuenta(Long id, PlanDeCuentasDTO cuentaDTO) throws IllegalArgumentException;

    /**
     * Elimina una cuenta.
     * @param id El ID de la cuenta a eliminar.
     * @throws IllegalArgumentException si la cuenta no existe o si tiene hijos.
     */
    void eliminarCuenta(Long id) throws IllegalArgumentException;
}
