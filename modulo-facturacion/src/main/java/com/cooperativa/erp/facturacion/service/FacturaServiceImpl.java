package com.cooperativa.erp.facturacion.service;

import com.cooperativa.erp.core.entity.ContratoServicio;
import com.cooperativa.erp.core.entity.Socio;
import com.cooperativa.erp.core.entity.Suministro;
import com.cooperativa.erp.electricidad.entity.*;
import com.cooperativa.erp.electricidad.repository.ConceptoFacturableRepository;
import com.cooperativa.erp.electricidad.repository.ContratoElectricidadRepository;
import com.cooperativa.erp.electricidad.service.LecturaService;
import com.cooperativa.erp.electricidad.service.PrecioConceptoService;
import com.cooperativa.erp.facturacion.entity.ComprobanteTipo;
import com.cooperativa.erp.facturacion.entity.Factura;
import com.cooperativa.erp.facturacion.entity.FacturaDetalle;
import com.cooperativa.erp.facturacion.entity.PuntoVenta;
import com.cooperativa.erp.facturacion.repository.FacturaRepository;
import com.cooperativa.erp.facturacion.repository.PuntoVentaRepository;
import com.cooperativa.erp.facturacion.repository.ComprobanteTipoRepository;


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

    // Repositorios propios (Facturación)
    private final FacturaRepository facturaRepository;
    private final PuntoVentaRepository puntoVentaRepository;
    private final ComprobanteTipoRepository comprobanteTipoRepository;

    // Repositorios de otros módulos
    private final ConceptoFacturableRepository conceptoFacturableRepository;
    private final ContratoElectricidadRepository contratoElectricidadRepository;

    // Servicios de otros módulos
    private final LecturaService lecturaService;
    private final PrecioConceptoService precioConceptoService;

    @Autowired
    private EntityManager entityManager;


    // Constructor actualizado
    public FacturaServiceImpl(FacturaRepository facturaRepository,
                              PuntoVentaRepository puntoVentaRepository,
                              ComprobanteTipoRepository comprobanteTipoRepository,
                              ConceptoFacturableRepository conceptoFacturableRepository,
                              ContratoElectricidadRepository contratoElectricidadRepository,
                              LecturaService lecturaService,
                              PrecioConceptoService precioConceptoService) {
        this.facturaRepository = facturaRepository;
        this.puntoVentaRepository = puntoVentaRepository;
        this.comprobanteTipoRepository = comprobanteTipoRepository;
        this.conceptoFacturableRepository = conceptoFacturableRepository;
        this.contratoElectricidadRepository = contratoElectricidadRepository;
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

        log.info("Iniciando generación de factura para Suministro NIS: {}, Período: {} - {}",
                suministro.getNis(), fechaDesde, fechaHasta);

        Socio socio = suministro.getSocio();
        if (socio == null) {
            throw new IllegalArgumentException("El suministro ID: " + suministro.getId() + " no tiene un socio asociado.");
        }
        log.debug("Socio: {} ({}) - Condición IVA: {}", socio.getNombreCompleto(), socio.getCuit(), socio.getCondicionIVA());

        // --- 1. Determinar Tipo de Comprobante ---
        ComprobanteTipo tipoComprobante = determinarTipoComprobante(socio.getCondicionIVA());
        log.debug("Tipo de Comprobante determinado: {}", tipoComprobante.getCodigoAfip());

        // --- 2. Obtener Contrato, Medidor y Categoría Tarifaria ---
        ContratoElectricidad contratoElec = contratoElectricidadRepository
                .findBySuministroAndEstado(suministro, ContratoServicio.EstadoContrato.ACTIVO)
                .orElseThrow(() -> new Exception("No se encontró un contrato de electricidad ACTIVO para el suministro " + suministro.getNis()));

        Medidor medidor = contratoElec.getMedidor();
        CategoriaTarifaria categoria = contratoElec.getCategoriaTarifaria();
        // *** CORRECCIÓN GETTER ***
        log.debug("Contrato activo encontrado. Medidor: {}, Categoría: {}", medidor.getNumero(), categoria.getCodigo());


        // --- 3. Obtener Lecturas y Calcular Consumo ---
        List<Lectura> lecturas = lecturaService.buscarLecturasPorRango(medidor.getId(), fechaDesde, fechaHasta);

        if (lecturas.size() < 2) {
            log.error("No se encontraron suficientes lecturas (inicial y final) para el período {} - {} en medidor {}", fechaDesde, fechaHasta, medidor.getNumero());
            throw new Exception("No se encontraron suficientes lecturas (inicial y final) para el período.");
        }

        Lectura lecturaInicial = lecturas.get(0);
        Lectura lecturaFinal = lecturas.get(lecturas.size() - 1);

        // *** CORRECCIÓN GETTER ***
        BigDecimal consumoCalculado = (lecturaFinal.getEstado().subtract(lecturaInicial.getEstado()))
                .multiply(medidor.getConstanteMultiplicacion());

        // *** CORRECCIÓN GETTER ***
        log.info("Consumo calculado: {} kWh ( ({} - {}) * {} )", consumoCalculado.setScale(2, RoundingMode.HALF_UP),
                lecturaFinal.getEstado(), lecturaInicial.getEstado(), medidor.getConstanteMultiplicacion());


        // --- 4. Obtener Precios Vigentes y Calcular Items ---
        // *** CORRECCIÓN GETTER ***
        List<PrecioConcepto> preciosVigentes = precioConceptoService.obtenerPreciosVigentes(categoria.getId(), fechaHasta);
        log.debug("Se encontraron {} precios vigentes para la categoría {} en fecha {}",
                preciosVigentes.size(), categoria.getCodigo(), fechaHasta);

        List<FacturaDetalle> detalles = new ArrayList<>();
        BigDecimal subtotalNeto = BigDecimal.ZERO;

        // --- 4a. Calcular Item: Consumo Eléctrico (KWh) ---
        ConceptoFacturable conceptoEnergia = conceptoFacturableRepository.findByCodigo("ELEC_KWH")
                .orElseThrow(() -> new RuntimeException("Concepto 'ELEC_KWH' no encontrado en la base de datos."));

        BigDecimal precioEnergia = preciosVigentes.stream()
                .filter(p -> p.getConceptoFacturable().equals(conceptoEnergia))
                .map(PrecioConcepto::getPrecioUnitario)
                .findFirst()
                // *** CORRECCIÓN GETTER ***
                .orElseThrow(() -> new Exception("No se encontró precio vigente para 'ELEC_KWH' en categoría " + categoria.getCodigo()));

        BigDecimal importeEnergia = consumoCalculado.multiply(precioEnergia).setScale(2, RoundingMode.HALF_UP);

        FacturaDetalle detalleEnergia = new FacturaDetalle();
        detalleEnergia.setConceptoFacturable(conceptoEnergia);
        detalleEnergia.setDescripcion(String.format("%s (%s kWh @ %s)", conceptoEnergia.getDescripcion(), consumoCalculado.toString(), precioEnergia.toString()));
        detalleEnergia.setCantidad(consumoCalculado);
        detalleEnergia.setPrecioUnitario(precioEnergia);
        detalleEnergia.setImporteNeto(importeEnergia);
        detalles.add(detalleEnergia);
        subtotalNeto = subtotalNeto.add(importeEnergia);


        // --- 4b. Calcular Item: Cargo Fijo ---
        ConceptoFacturable conceptoCargoFijo = conceptoFacturableRepository.findByCodigo("CARGO_FIJO")
                .orElseThrow(() -> new RuntimeException("Concepto 'CARGO_FIJO' no encontrado en la base de datos."));

        BigDecimal precioCargoFijo = preciosVigentes.stream()
                .filter(p -> p.getConceptoFacturable().equals(conceptoCargoFijo))
                .map(PrecioConcepto::getPrecioUnitario)
                .findFirst()
                // *** CORRECCIÓN GETTER ***
                .orElseThrow(() -> new Exception("No se encontró precio vigente para 'CARGO_FIJO' en categoría " + categoria.getCodigo()));

        FacturaDetalle detalleCargoFijo = new FacturaDetalle();
        detalleCargoFijo.setConceptoFacturable(conceptoCargoFijo);
        detalleCargoFijo.setDescripcion(conceptoCargoFijo.getDescripcion());
        detalleCargoFijo.setCantidad(BigDecimal.ONE);
        detalleCargoFijo.setPrecioUnitario(precioCargoFijo);
        detalleCargoFijo.setImporteNeto(precioCargoFijo);
        detalles.add(detalleCargoFijo);
        subtotalNeto = subtotalNeto.add(precioCargoFijo);


        // --- 5. Calcular Impuestos Globales (IVA, etc.) ---
        BigDecimal totalImpuestos;
        if (socio.getCondicionIVA() == Socio.CondicionIVA.RESPONSABLE_INSCRIPTO) {
            totalImpuestos = subtotalNeto.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP);
        } else {
            totalImpuestos = subtotalNeto.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal totalFactura = subtotalNeto.add(totalImpuestos);


        // --- 6. Crear Objeto Factura ---
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
        factura.setImporteIVA(totalImpuestos);
        factura.setImporteTotal(totalFactura);
        factura.setEstado("PENDIENTE");

        for(FacturaDetalle detalle : detalles) {
            factura.addDetalle(detalle);
        }

        log.info("Objeto Factura pre-generado para Suministro NIS {}. Total: {}", suministro.getNis(), factura.getImporteTotal());

        return factura;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public long obtenerProximoNumeroComprobante(PuntoVenta puntoVenta, ComprobanteTipo comprobanteTipo) {
        log.debug("Obteniendo próximo número para PV {} y Tipo {}", puntoVenta.getNumero(), comprobanteTipo.getCodigoAfip());

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

        PuntoVenta pvBloqueado = entityManager.find(PuntoVenta.class, puntoVenta.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (pvBloqueado == null) {
            throw new RuntimeException("Punto de Venta no encontrado o bloqueo fallido al guardar: ID " + puntoVenta.getId());
        }

        long numeroAsignar = obtenerProximoNumeroComprobante(pvBloqueado, factura.getComprobanteTipo());
        factura.setNumeroComprobante(numeroAsignar);
        log.debug("Número {} asignado a la factura", numeroAsignar);

        Factura facturaGuardada = facturaRepository.save(factura);
        log.info("Factura ID {} guardada con Número {}", facturaGuardada.getId(), facturaGuardada.getNumeroComprobante());

        pvBloqueado.setUltimoNumeroEmitido(numeroAsignar);
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
                codigoAfip = "001";
                break;
            case CONSUMIDOR_FINAL:
            case MONOTRIBUTO:
            case EXENTO:
            default:
                codigoAfip = "006";
                break;
        }
        return comprobanteTipoRepository.findByCodigoAfip(codigoAfip)
                .orElseThrow(() -> new RuntimeException("Tipo de Comprobante AFIP no encontrado: " + codigoAfip));
    }

}

