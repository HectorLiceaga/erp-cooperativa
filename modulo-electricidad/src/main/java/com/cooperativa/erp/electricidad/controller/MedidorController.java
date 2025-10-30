package com.cooperativa.erp.controller.electricidad;

import com.cooperativa.erp.controller.electricidad.dto.AsignarSuministroRequestDTO;
import com.cooperativa.erp.controller.electricidad.dto.MedidorDTO;
import com.cooperativa.erp.electricidad.entity.Medidor; // Importamos la entidad
import com.cooperativa.erp.electricidad.service.MedidorService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils; // Para copiar propiedades
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // Para errores HTTP

import java.util.Optional;

@RestController
@RequestMapping("/api/electricidad/medidores") // Ruta base para medidores
public class MedidorController {

    private final MedidorService medidorService;

    // Inyección por constructor (mejor práctica)
    public MedidorController(MedidorService medidorService) {
        this.medidorService = medidorService;
    }

    @PostMapping
    public ResponseEntity<MedidorDTO> crearMedidor(@Valid @RequestBody MedidorDTO medidorDTO) {
        try {
            // Convertimos DTO a Entidad (simplificado, podríamos usar MapStruct)
            Medidor nuevoMedidor = new Medidor();
            BeanUtils.copyProperties(medidorDTO, nuevoMedidor, "id", "suministroId", "suministroDireccion"); // Ignoramos campos que no van en la creación directa

            Medidor medidorGuardado = medidorService.crearOActualizarMedidor(nuevoMedidor);

            // Convertimos Entidad guardada a DTO para la respuesta
            MedidorDTO respuestaDTO = convertirAElectricidadDTO(medidorGuardado);
            return ResponseEntity.status(HttpStatus.CREATED).body(respuestaDTO);
        } catch (Exception e) {
            // Aquí podríamos loggear el error real e.getMessage()
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al crear medidor: " + e.getMessage());
        }
    }

    @GetMapping("/{numero}")
    public ResponseEntity<MedidorDTO> buscarPorNumero(@PathVariable String numero) {
        Optional<Medidor> medidorOpt = medidorService.buscarPorNumero(numero);
        if (medidorOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medidor no encontrado con número: " + numero);
        }
        return ResponseEntity.ok(convertirAElectricidadDTO(medidorOpt.get()));
    }

    @PutMapping("/{medidorId}/asignar-suministro")
    public ResponseEntity<MedidorDTO> asignarSuministro(@PathVariable Long medidorId, @Valid @RequestBody AsignarSuministroRequestDTO requestDTO) {
        try {
            Medidor medidorActualizado = medidorService.asignarSuministro(medidorId, requestDTO.getSuministroId());
            return ResponseEntity.ok(convertirAElectricidadDTO(medidorActualizado));
        } catch (Exception e) { // Captura excepciones del servicio (ej. MedidorNoEncontrado, SuministroNoEncontrado)
            // Deberíamos tener excepciones personalizadas y un @ControllerAdvice
            // Por ahora, lanzamos errores genéricos basados en el mensaje
            if (e.getMessage().contains("Medidor no encontrado")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } else if (e.getMessage().contains("Suministro no encontrado")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } else if (e.getMessage().contains("ya está asociado")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()); // 409 Conflict
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al asignar suministro: " + e.getMessage());
        }
    }


    // --- Métodos Helper ---

    // Convertidor simple de Entidad a DTO (Idealmente usar MapStruct o similar)
    private MedidorDTO convertirAElectricidadDTO(Medidor medidor) {
        MedidorDTO dto = new MedidorDTO();
        BeanUtils.copyProperties(medidor, dto);
        if (medidor.getSuministro() != null) {
            dto.setSuministroId(medidor.getSuministro().getId());
            // Podríamos buscar la dirección del suministro para mostrarla
            // dto.setSuministroDireccion(medidor.getSuministro().getDireccion()); // Asumiendo que Suministro tiene direccion
        }
        return dto;
    }
}

