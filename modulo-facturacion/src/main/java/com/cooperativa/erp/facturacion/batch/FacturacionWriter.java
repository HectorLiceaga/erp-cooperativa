package com.cooperativa.erp.facturacion.batch;

import com.cooperativa.erp.electricidad.entity.Lectura;
import com.cooperativa.erp.electricidad.service.LecturaService;
import com.cooperativa.erp.facturacion.entity.Factura;
import com.cooperativa.erp.facturacion.service.FacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Nombre de archivo CORREGIDO (FacturacionWriter.java)
 * Escribe el lote (Chunk) de Facturas generadas.
 */
public class FacturacionWriter implements ItemWriter<Factura> {

    private static final Logger log = LoggerFactory.getLogger(FacturacionWriter.class);

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private LecturaService lecturaService;

    @Override
    @Transactional // El Writer debe ser transaccional por CHUNK
    public void write(Chunk<? extends Factura> chunk) throws Exception {

        // CORREGIDO: Manejar el tipo genérico del Chunk de forma segura
        List<Factura> facturas = (List<Factura>) chunk.getItems();

        log.info("Iniciando escritura de Lote (Chunk) de {} facturas.", facturas.size());

        for (Factura factura : facturas) {
            try {
                // CORREGIDO: Usar el getter .getPuntoVenta() (que Lombok generará)
                Factura facturaGuardada = facturaService.guardarFacturaYActualizarPuntoVenta(
                        factura,
                        factura.getPuntoVenta()
                );

                // CORREGIDO: Usar getters .getId() y .getNumeroComprobante() (Lombok)
                log.debug("Factura guardada ID: {}, Número: {}",
                        facturaGuardada.getId(), facturaGuardada.getNumeroComprobante());

            } catch (Exception e) {
                // Si falla una escritura, la transacción del chunk hará rollback
                // CORREGIDO: Usar getter .getSocio() y .getCuit() (Lombok)
                log.error("Error crítico al guardar factura para Socio {}: {}. Haciendo rollback del Lote.",
                        factura.getSocio().getCuit(), e.getMessage(), e);
                // Relanzamos la excepción para que el Batch falle y haga rollback
                throw e;
            }
        }

        // Si todo el lote se guardó OK, marcamos las lecturas como facturadas

        // CORREGIDO: Usar la referencia al método .getLecturaFinal() (Lombok)
        List<Lectura> lecturasProcesadas = facturas.stream()
                .map(Factura::getLecturaFinal) // Usa el @Transient que añadimos a Factura.java
                .collect(Collectors.toList());

        if (!lecturasProcesadas.isEmpty()) {
            lecturaService.marcarLecturasComoFacturadas(lecturasProcesadas);
            log.debug("Marcadas {} lecturas como 'Facturada=true'", lecturasProcesadas.size());
        }

        log.info("Lote (Chunk) de {} facturas escrito exitosamente.", facturas.size());
    }
}

