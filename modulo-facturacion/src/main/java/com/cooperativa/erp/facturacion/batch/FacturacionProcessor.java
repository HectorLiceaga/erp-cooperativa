package com.cooperativa.erp.facturacion.batch;

import com.cooperativa.erp.electricidad.entity.Lectura;
import com.cooperativa.erp.facturacion.entity.Factura;
import com.cooperativa.erp.facturacion.entity.PuntoVenta;
import com.cooperativa.erp.facturacion.repository.PuntoVentaRepository;
import com.cooperativa.erp.facturacion.service.FacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FacturacionProcessor implements ItemProcessor<Lectura, Factura> {

    private static final Logger log = LoggerFactory.getLogger(FacturacionProcessor.class);

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private PuntoVentaRepository puntoVentaRepository;

    private final String vencimientoStr;
    // --- CORRECCIÓN DE TIPO ---
    private final Integer puntoVentaId; // Cambiado a Integer

    private LocalDate fechaVencimiento;
    private PuntoVenta puntoVenta; // Cachear el PV

    /**
     * Constructor para pasar parámetros del Job.
     * // --- CORRECCIÓN DE TIPO ---
     */
    public FacturacionProcessor(String vencimientoStr, Integer puntoVentaId) { // Cambiado a Integer
        if (vencimientoStr == null || puntoVentaId == null) {
            throw new IllegalArgumentException("vencimientoStr y puntoVentaId no pueden ser nulos");
        }
        this.vencimientoStr = vencimientoStr;
        this.puntoVentaId = puntoVentaId;
    }

    /**
     * Lógica de procesamiento principal
     */
    @Override
    public Factura process(Lectura lecturaFinal) throws Exception {

        // Inicializar parámetros cacheados en el primer item
        if (this.puntoVenta == null) {
            this.fechaVencimiento = LocalDate.parse(vencimientoStr, DateTimeFormatter.ISO_DATE);

            // Ahora 'puntoVentaId' es Integer, y findById(Integer) funcionará.
            this.puntoVenta = puntoVentaRepository.findById(puntoVentaId)
                    .orElseThrow(() -> new RuntimeException("Parámetro de Job 'puntoVentaId' inválido: " + puntoVentaId));

            log.info("FacturacionProcessor iniciado. PV: {}, Vencimiento: {}",
                    this.puntoVenta.getNumero(), fechaVencimiento);
        }

        Long contratoId = lecturaFinal.getContratoElectricidad().getId();
        log.debug("Procesando Lectura ID: {} para Contrato ID: {}", lecturaFinal.getId(), contratoId);

        try {
            Factura facturaGenerada = facturaService.generarFacturaDesdeLectura(
                    lecturaFinal,
                    this.fechaVencimiento,
                    this.puntoVenta
            );

            log.debug("Factura pre-generada para Contrato ID {}. Total: {}", contratoId, facturaGenerada.getImporteTotal());
            return facturaGenerada;

        } catch (Exception e) {
            // Si falla la generación de una factura, logueamos y saltamos (retornando null)
            log.error("Error al procesar Lectura ID {}: {}. Se saltará este item.", lecturaFinal.getId(), e.getMessage(), e);
            return null; // Spring Batch filtrará este item
        }
    }
}

