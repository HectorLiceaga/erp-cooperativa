package com.cooperativa.erp.facturacion.service;

import com.cooperativa.erp.core.entity.Socio;
import com.cooperativa.erp.core.entity.Suministro;
// TODO: Necesitaremos importar entidades de Contrato/CategoriaTarifaria cuando las creemos
import com.cooperativa.erp.electricidad.entity.ConceptoFacturable;
// import com.cooperativa.erp.electricidad.entity.Lectura; // No usada directamente aquí aún
import com.cooperativa.erp.electricidad.entity.PrecioConcepto;
import com.cooperativa.erp.electricidad.repository.ConceptoFacturableRepository;
import com.cooperativa.erp.electricidad.service.LecturaService;
import com.cooperativa.erp.electricidad.service.PrecioConceptoService;
// *** IMPORTS CORREGIDOS ***
import com.cooperativa.erp.facturacion.entity.ComprobanteTipo;
import com.cooperativa.erp.facturacion.entity.Factura;
import com.cooperativa.erp.facturacion.entity.FacturaDetalle;
import com.cooperativa.erp.facturacion.entity.PuntoVenta;
import com.cooperativa.erp.facturacion.repository.FacturaRepository;
import com.cooperativa.erp.facturacion.repository.PuntoVentaRepository;
import com.cooperativa.erp.facturacion.repository.ComprobanteTipoRepository;
// *** FIN IMPORTS CORREGIDOS ***

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FacturaServiceImpl implements FacturaService {

    private static final Logger log = LoggerFactory.getLogger(FacturaServiceImpl.class);

    private final FacturaRepository facturaRepository;
    private final PuntoVentaRepository puntoVentaRepository;
    private final ComprobanteTipoRepository comprobanteTipoRepository;
    private final ConceptoFacturableRepository conceptoFacturableRepository;

    // Servicios de otros módulos
    private final LecturaService lecturaService;
    private final PrecioConceptoService precioConceptoService;

    // Necesitamos EntityManager para bloqueos
    @Autowired
    private EntityManager entityManager;


    // Inyección por constructor
    public FacturaServiceImpl(FacturaRepository facturaRepository,
                              PuntoVentaRepository puntoVentaRepository,
                              ComprobanteTipoRepository comprobanteTipoRepository,
                              ConceptoFacturableRepository conceptoFacturableRepository,
                              LecturaService lecturaService,
                              PrecioConceptoService precioConceptoService) {
        this.facturaRepository = facturaRepository;
        this.puntoVentaRepository = puntoVentaRepository;
        this.comprobanteTipoRepository = comprobanteTipoRepository;
        this.conceptoFacturableRepository = conceptoFacturableRepository;
        this.lecturaService = lecturaService;
        this.precioConceptoService = precioConceptoService;
    }

    @Override
    @Transactional(readOnly = true)
    public Factura generarFacturaPeriodo(Suministro suministro,
                                         LocalDate fechaDesde,
                                         LocalDate fechaHasta,
                                         LocalDate fechaVencimiento,
                                         PuntoVenta puntoVenta) throws Exception {

        log.info("Iniciando generación de factura para Suministro ID: {}, Período: {} - {}",
                suministro.getId(), fechaDesde, fechaHasta);

        Socio socio = suministro.getSocio();
        if (socio == null) {
            throw new IllegalArgumentException("El suministro ID: " + suministro.getId() + " no tiene un socio asociado.");
        }
        log.debug("Socio: {} ({}) - Condición IVA: {}", socio.getNombreCompleto(), socio.getCuit(), socio.getCondicionIVA());

        // --- 1. Determinar Tipo de Comprobante (Ej. A o B según Condición IVA) ---
        ComprobanteTipo tipoComprobante = determinarTipoComprobante(socio.getCondicionIVA());
        log.debug("Tipo de Comprobante determinado: {}", tipoComprobante.getCodigoAfip());


        // --- 2. Obtener Próximo Número ---
        log.debug("Se usará el Punto de Venta {} para el tipo {}", puntoVenta.getNumero(), tipoComprobante.getCodigoAfip());

        // --- 3. Obtener Lecturas y Calcular Consumo ---
        // TODO: Implementar lógica detallada aquí.
        BigDecimal consumoCalculado = new BigDecimal("150.75"); // Placeholder
        log.warn("Cálculo de consumo es un placeholder: {} kWh", consumoCalculado);

        // --- 4. Obtener Precios Vigentes y Calcular Items ---
        Long categoriaTarifariaId = 1L; // Placeholder
        List<PrecioConcepto> preciosVigentes = precioConceptoService.obtenerPreciosVigentes(categoriaTarifariaId, fechaHasta);
        log.debug("Se encontraron {} precios vigentes para la categoría {} en fecha {}",
                preciosVigentes.size(), categoriaTarifariaId, fechaHasta);

        List<FacturaDetalle> detalles = new ArrayList<>();
        BigDecimal subtotalNeto = BigDecimal.ZERO;

        // Placeholder para detalle de consumo:
        ConceptoFacturable conceptoEnergia = conceptoFacturableRepository.findByCodigo("ELEC_KWH").orElseThrow(() -> new RuntimeException("Concepto ELEC_KWH no encontrado"));
        BigDecimal precioEnergia = preciosVigentes.stream()
                .filter(p -> p.getConceptoFacturable().equals(conceptoEnergia))
                .map(PrecioConcepto::getPrecioUnitario)
                .findFirst().orElse(new BigDecimal("25.50")); // Precio placeholder
        BigDecimal importeEnergia = consumoCalculado.multiply(precioEnergia).setScale(2, RoundingMode.HALF_UP);

        // *** USAR LA CLASE CORRECTA ***
        FacturaDetalle detalleEnergia = new FacturaDetalle();
        detalleEnergia.setConceptoFacturable(conceptoEnergia);
        detalleEnergia.setDescripcion(conceptoEnergia.getDescripcion() + " (" + consumoCalculado + " kWh @ " + precioEnergia + ")");
        detalleEnergia.setCantidad(consumoCalculado);
        detalleEnergia.setPrecioUnitario(precioEnergia);
        detalleEnergia.setImporteNeto(importeEnergia);
        detalleEnergia.setAlicuotaIVA(new BigDecimal("21.00")); // Placeholder
        detalleEnergia.setImporteIVA(importeEnergia.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP)); // Placeholder
        detalleEnergia.setImporteTotal(detalleEnergia.getImporteNeto().add(detalleEnergia.getImporteIVA()));
        detalles.add(detalleEnergia);
        subtotalNeto = subtotalNeto.add(detalleEnergia.getImporteNeto());

        log.warn("Cálculo de detalles es un placeholder.");

        // --- 5. Calcular Impuestos Globales (IVA sobre neto, Percepciones, etc.) ---
        BigDecimal totalImpuestos = detalleEnergia.getImporteIVA(); // Placeholder
        BigDecimal totalFactura = detalleEnergia.getImporteTotal(); // Placeholder
        log.warn("Cálculo de impuestos es un placeholder.");

        // --- 6. Crear Objeto Factura ---
        // *** USAR LA CLASE CORRECTA ***
        Factura factura = new Factura();
        factura.setSocio(socio);
        factura.setSuministro(suministro);
        factura.setPuntoVenta(puntoVenta);
        factura.setComprobanteTipo(tipoComprobante);
        factura.setFechaEmision(LocalDate.now());
        factura.setPeriodoDesde(fechaDesde);
        factura.setPeriodoHasta(fechaHasta);
        factura.setFechaVencimiento(fechaVencimiento);
        factura.setImporteNeto(subtotalNeto);
        factura.setImporteIVA(totalImpuestos); // Simplificación placeholder
        factura.setImporteTotal(totalFactura);
        factura.setEstado("PENDIENTE"); // Estado inicial

        for(FacturaDetalle detalle : detalles) {
            factura.addDetalle(detalle);
        }

        log.info("Objeto Factura pre-generado para Suministro ID: {}", suministro.getId());

        return factura;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public long obtenerProximoNumeroComprobante(PuntoVenta puntoVenta, ComprobanteTipo comprobanteTipo) {
        log.debug("Obteniendo próximo número para PV {} y Tipo {}", puntoVenta.getNumero(), comprobanteTipo.getCodigoAfip());

        // *** USAR LA CLASE CORRECTA ***
        PuntoVenta pvBloqueado = entityManager.find(PuntoVenta.class, puntoVenta.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (pvBloqueado == null) {
            throw new RuntimeException("Punto de Venta no encontrado o bloqueo fallido: ID " + puntoVenta.getId());
        }

        Optional<Factura> ultimaFacturaOpt = facturaRepository.findTopByPuntoVentaAndComprobanteTipoOrderByNumeroComprobanteDesc(
                pvBloqueado, comprobanteTipo);

        long proximoNumero = ultimaFacturaOpt.map(factura -> factura.getNumeroComprobante() + 1).orElse(1L);

        log.info("Próximo número determinado para PV {}: {}", pvBloqueado.getNumero(), proximoNumero);
        return proximoNumero;
    }


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Factura guardarFacturaYActualizarPuntoVenta(Factura factura, PuntoVenta puntoVenta) {
        log.info("Intentando guardar factura y actualizar PV {}", puntoVenta.getNumero());

        // *** USAR LA CLASE CORRECTA ***
        PuntoVenta pvBloqueado = entityManager.find(PuntoVenta.class, puntoVenta.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (pvBloqueado == null) {
            throw new RuntimeException("Punto de Venta no encontrado o bloqueo fallido al guardar: ID " + puntoVenta.getId());
        }

        long numeroAsignar = obtenerProximoNumeroComprobante(pvBloqueado, factura.getComprobanteTipo());
        factura.setNumeroComprobante(numeroAsignar);
        log.debug("Número {} asignado a la factura", numeroAsignar);

        Factura facturaGuardada = facturaRepository.save(factura);
        log.info("Factura ID {} guardada con Número {}", facturaGuardada.getId(), facturaGuardada.getNumeroComprobante());

        pvBloqueado.setUltimoNumeroEmitido(numeroAsignar); // Campo placeholder
        puntoVentaRepository.save(pvBloqueado);
        log.info("Último número actualizado en PV {} a {}", pvBloqueado.getNumero(), numeroAsignar);

        log.warn("Falta implementar registro en Cuenta Corriente");

        return facturaGuardada;
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Factura> buscarPorId(Long id) {
        log.debug("Buscando factura por ID: {}", id);
        return facturaRepository.findById(id);
    }

    private ComprobanteTipo determinarTipoComprobante(Socio.CondicionIVA condicionIVA) {
        String codigoAfip;
        switch (condicionIVA) {
            case RESPONSABLE_INSCRIPTO:
                codigoAfip = "001"; // Factura A
                break;
            case CONSUMIDOR_FINAL:
            case MONOTRIBUTO:
            case EXENTO:
            default:
                codigoAfip = "006"; // Factura B
                break;
        }
        return comprobanteTipoRepository.findByCodigoAfip(codigoAfip)
                .orElseThrow(() -> new RuntimeException("Tipo de Comprobante AFIP no encontrado: " + codigoAfip));
    }
}

