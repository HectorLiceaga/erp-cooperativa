package com.cooperativa.erp.contabilidad.service;

import com.cooperativa.erp.contabilidad.dto.PlanDeCuentasDTO;
import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;
import com.cooperativa.erp.contabilidad.repository.PlanDeCuentasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlanDeCuentasServiceImpl implements PlanDeCuentasService {

    private final PlanDeCuentasRepository planDeCuentasRepository;

    public PlanDeCuentasServiceImpl(PlanDeCuentasRepository planDeCuentasRepository) {
        this.planDeCuentasRepository = planDeCuentasRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanDeCuentasDTO> getArbolDeCuentasCompleto() {
        // Busca solo las cuentas raíz (las que no tienen padre)
        return planDeCuentasRepository.findByPadreId(null).stream()
                .map(cuenta -> new PlanDeCuentasDTO(cuenta, true)) // El DTO construye el árbol recursivamente
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanDeCuentasDTO> getCuentaById(Long id) {
        return planDeCuentasRepository.findById(id)
                .map(cuenta -> new PlanDeCuentasDTO(cuenta, true)); // Devuelve la cuenta y su sub-árbol
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanDeCuentasDTO> getCuentaByCodigo(String codigo) {
        return planDeCuentasRepository.findByCodigo(codigo)
                .map(cuenta -> new PlanDeCuentasDTO(cuenta, false)); // No necesitamos hijos para esto
    }

    @Override
    @Transactional
    public PlanDeCuentasDTO crearCuenta(PlanDeCuentasDTO cuentaDTO) throws IllegalArgumentException {
        // Validación 1: Código único
        if (planDeCuentasRepository.findByCodigo(cuentaDTO.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("El código de cuenta '" + cuentaDTO.getCodigo() + "' ya existe.");
        }

        PlanDeCuentas padre = null;
        if (cuentaDTO.getPadreId() != null) {
            padre = planDeCuentasRepository.findById(cuentaDTO.getPadreId())
                    .orElseThrow(() -> new IllegalArgumentException("La cuenta padre con ID " + cuentaDTO.getPadreId() + " no existe."));

            // Validación 2: El padre NO debe ser imputable
            if (padre.getImputable()) {
                throw new IllegalArgumentException("La cuenta padre '" + padre.getDescripcion() + "' es imputable. No puede tener hijos.");
            }
        }

        PlanDeCuentas nuevaCuenta = new PlanDeCuentas(
                cuentaDTO.getCodigo(),
                cuentaDTO.getDescripcion(),
                cuentaDTO.getImputable(),
                padre
        );

        PlanDeCuentas cuentaGuardada = planDeCuentasRepository.save(nuevaCuenta);
        return new PlanDeCuentasDTO(cuentaGuardada, false); // Devolvemos el DTO sin hijos
    }

    @Override
    @Transactional
    public PlanDeCuentasDTO actualizarCuenta(Long id, PlanDeCuentasDTO cuentaDTO) throws IllegalArgumentException {
        PlanDeCuentas cuentaExistente = planDeCuentasRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta con ID " + id + " no existe."));

        // Validación de código (si cambió)
        if (!cuentaExistente.getCodigo().equals(cuentaDTO.getCodigo())) {
            if (planDeCuentasRepository.findByCodigo(cuentaDTO.getCodigo()).isPresent()) {
                throw new IllegalArgumentException("El código de cuenta '" + cuentaDTO.getCodigo() + "' ya pertenece a otra cuenta.");
            }
        }

        // Lógica de validación de árbol (ej. no moverse a un padre inválido)
        // ... (Se puede añadir más lógica aquí) ...

        cuentaExistente.setCodigo(cuentaDTO.getCodigo());
        cuentaExistente.setDescripcion(cuentaDTO.getDescripcion());
        cuentaExistente.setImputable(cuentaDTO.getImputable());

        // (Simplificación: No permitimos re-parentar cuentas fácilmente, es complejo)

        PlanDeCuentas cuentaActualizada = planDeCuentasRepository.save(cuentaExistente);
        return new PlanDeCuentasDTO(cuentaActualizada, false);
    }

    @Override
    @Transactional
    public void eliminarCuenta(Long id) throws IllegalArgumentException {
        PlanDeCuentas cuenta = planDeCuentasRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta con ID " + id + " no existe."));

        // Validación: No se puede borrar si tiene hijos
        if (cuenta.getHijos() != null && !cuenta.getHijos().isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar la cuenta '" + cuenta.getDescripcion() + "' porque tiene cuentas hijas.");
        }

        // TODO: Validación futura: No se puede borrar si tiene asientos imputados.
        // (Esto requeriría inyectar AsientoRepository y chequear)

        planDeCuentasRepository.delete(cuenta);
    }
}
