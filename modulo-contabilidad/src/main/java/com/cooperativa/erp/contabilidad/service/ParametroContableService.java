package com.cooperativa.erp.contabilidad.service;

import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;

/**
 * Servicio para gestionar la parametrización contable.
 * Abstrae la lógica de buscar cuentas parametrizadas.
 */
public interface ParametroContableService {

    /**
     * Obtiene la cuenta contable asociada a una clave de negocio.
     * @param clave La clave (ej. "CTA_IVA_DEBITO")
     * @return La entidad PlanDeCuentas
     * @throws RuntimeException si el parámetro no está configurado (error crítico de setup).
     */
    PlanDeCuentas getCuentaByClave(String clave) throws RuntimeException;

    // TODO: Añadir métodos para el ABM de estos parámetros (crear, actualizar).
}
