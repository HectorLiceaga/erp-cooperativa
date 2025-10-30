package com.cooperativa.erp.contabilidad.service;

import com.cooperativa.erp.contabilidad.dto.PlanDeCuentasDTO;
import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;
import com.cooperativa.erp.contabilidad.repository.PlanDeCuentasRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlanDeCuentasServiceImpl implements PlanDeCuentasService {

    private static final Logger log = LoggerFactory.getLogger(PlanDeCuentasServiceImpl.class);

    private final PlanDeCuentasRepository planDeCuentasRepository;

    public PlanDeCuentasServiceImpl(PlanDeCuentasRepository planDeCuentasRepository) {
        this.planDeCuentasRepository = planDeCuentasRepository;
    }

    /**
     * ¡MÉTODO RENOMBRADO! (Antes 'obtenerTodas')
     * Obtiene el árbol de cuentas completo desde la raíz.
     * @return Lista de cuentas de primer nivel (padres nulos) con sus hijos anidados.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlanDeCuentasDTO> getArbolDeCuentasCompleto() {
        // NOTA: Esto sigue siendo un findAll(), la lógica de árbol
        // real (hijos recursivos) la implementaremos más adelante.
        return planDeCuentasRepository.findAll().stream()
                .map(PlanDeCuentasDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * ¡MÉTODO AÑADIDO QUE FALTABA!
     * Obtiene una cuenta específica por su ID, con sus hijos.
     * @param id El ID de la cuenta.
     * @return Un DTO de la cuenta con su sub-árbol.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<PlanDeCuentasDTO> getCuentaById(Long id) {
        log.debug("Buscando Cuenta por ID: {}", id);
        return planDeCuentasRepository.findById(id)
                .map(PlanDeCuentasDTO::new);
    }


    /**
     * ¡MÉTODO RENOMBRADO! (Antes 'obtenerPorCodigo')
     * Obtiene una cuenta por su código de imputación.
     * @param codigo Ej: "1.01.01.001"
     * @return El DTO de la cuenta.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<PlanDeCuentasDTO> getCuentaByCodigo(String codigo) {
        return planDeCuentasRepository.findByCodigo(codigo)
                .map(PlanDeCuentasDTO::new);
    }

    @Override
    @Transactional
    public PlanDeCuentasDTO crearCuenta(PlanDeCuentasDTO dto) throws IllegalArgumentException {
        log.info("Creando nueva cuenta contable con código {}", dto.getCodigo());
        if (planDeCuentasRepository.findByCodigo(dto.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta con el código " + dto.getCodigo());
        }

        PlanDeCuentas padre = null;
        if (dto.getPadreId() != null) {
            padre = planDeCuentasRepository.findById(dto.getPadreId())
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró la cuenta padre con ID " + dto.getPadreId()));

            if (padre.getImputable()) {
                log.warn("Validación fallida: La cuenta padre {} ({}) es imputable y no puede tener hijas.", padre.getCodigo(), padre.getNombre());
                throw new IllegalArgumentException("La cuenta padre " + padre.getNombre() + " es imputable y no puede tener hijas.");
            }
        }

        PlanDeCuentas nuevaCuenta = new PlanDeCuentas();
        nuevaCuenta.setCodigo(dto.getCodigo());
        nuevaCuenta.setNombre(dto.getNombre());
        nuevaCuenta.setImputable(dto.getImputable());
        nuevaCuenta.setCuentaPadre(padre);

        PlanDeCuentas guardada = planDeCuentasRepository.save(nuevaCuenta);
        log.info("Cuenta {} guardada con ID {}", guardada.getCodigo(), guardada.getId());
        return new PlanDeCuentasDTO(guardada);
    }

    @Override
    @Transactional
    public PlanDeCuentasDTO actualizarCuenta(Long id, PlanDeCuentasDTO dto) {
        PlanDeCuentas cuentaExistente = planDeCuentasRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la cuenta con ID " + id));

        cuentaExistente.setNombre(dto.getNombre());
        cuentaExistente.setImputable(dto.getImputable());
        // (Omitimos cambiar el código y el padre en una actualización simple)

        PlanDeCuentas actualizada = planDeCuentasRepository.save(cuentaExistente);
        return new PlanDeCuentasDTO(actualizada);
    }

    @Override
    @Transactional
    public void eliminarCuenta(Long id) {
        PlanDeCuentas cuenta = planDeCuentasRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la cuenta con ID " + id));

        if (cuenta.getCuentasHijas() != null && !cuenta.getCuentasHijas().isEmpty()) {
            log.warn("Eliminación fallida: La cuenta {} ({}) tiene cuentas hijas.", cuenta.getCodigo(), cuenta.getNombre());
            throw new IllegalStateException("No se puede eliminar la cuenta " + cuenta.getNombre() + " porque tiene cuentas hijas.");
        }

        // TODO: Añadir validación de que la cuenta no tenga movimientos en AsientoDetalle

        planDeCuentasRepository.delete(cuenta);
        log.info("Cuenta con ID {} eliminada.", id);
    }
}