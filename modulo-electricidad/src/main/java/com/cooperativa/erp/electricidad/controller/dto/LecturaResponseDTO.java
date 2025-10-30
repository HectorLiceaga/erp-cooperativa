package com.cooperativa.erp.electricidad.dto;

import com.cooperativa.erp.electricidad.entity.Lectura;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para devolver una Lectura recién creada.
 * Resuelve los errores de 'getMedidor', etc.
 */
@Data
public class LecturaResponseDTO {

    private Long idLectura;
    private Long contratoId;
    private Long medidorId; // Obtenido del contrato
    private String medidorNumero; // Obtenido del contrato
    private LocalDate fechaToma;
    private LocalDate fechaPeriodo;
    private BigDecimal estadoAnterior;
    private BigDecimal estadoActual;
    private BigDecimal consumoKwh;
    private String tipoLectura;
    private boolean facturada;

    /**
     * Mapeador estático para convertir la Entidad (JPA) a este DTO (API).
     */
    public static LecturaResponseDTO fromEntity(Lectura lectura) {
        LecturaResponseDTO dto = new LecturaResponseDTO();
        dto.setIdLectura(lectura.getId());

        // Aquí está la magia: usamos los getters correctos de la entidad
        if (lectura.getContratoElectricidad() != null) {
            dto.setContratoId(lectura.getContratoElectricidad().getId());
            // Asumiendo que ContratoElectricidad tiene un Medidor
            if (lectura.getContratoElectricidad().getMedidor() != null) {
                dto.setMedidorId(lectura.getContratoElectricidad().getMedidor().getId());
                dto.setMedidorNumero(lectura.getContratoElectricidad().getMedidor().getNumero());
            }
        }

        dto.setFechaToma(lectura.getFechaToma());
        dto.setFechaPeriodo(lectura.getFechaPeriodo());
        dto.setEstadoAnterior(lectura.getEstadoAnterior());
        dto.setEstadoActual(lectura.getEstadoActual());
        dto.setConsumoKwh(lectura.getConsumoKwh());
        dto.setTipoLectura(lectura.getTipoLectura());
        dto.setFacturada(lectura.isFacturada());

        return dto;
    }
}
