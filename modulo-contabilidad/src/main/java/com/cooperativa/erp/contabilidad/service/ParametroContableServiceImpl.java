package com.cooperativa.erp.contabilidad.service;

import com.cooperativa.erp.contabilidad.entity.ParametroContable;
import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;
import com.cooperativa.erp.contabilidad.repository.ParametroContableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParametroContableServiceImpl implements ParametroContableService {

    private static final Logger log = LoggerFactory.getLogger(ParametroContableServiceImpl.class);

    private final ParametroContableRepository parametroRepository;

    public ParametroContableServiceImpl(ParametroContableRepository parametroRepository) {
        this.parametroRepository = parametroRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PlanDeCuentas getCuentaByClave(String clave) throws RuntimeException {
        log.debug("Buscando parámetro contable para clave: {}", clave);

        return parametroRepository.findByClave(clave)
                .map(ParametroContable::getCuenta)
                .orElseThrow(() -> {
                    log.error("¡PARAMETRIZACIÓN FALTANTE! No se encontró la clave contable: {}", clave);
                    // Esto es un error crítico de configuración del ERP.
                    return new RuntimeException("Error de configuración: Parámetro contable no definido: " + clave);
                });
    }
}
