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

    // Repositorios
    private final FacturaRepository facturaRepository;
    private final PuntoVentaRepository puntoVentaRepository;
    private final ComprobanteTipoRepository comprobanteTipoRepository;
    private final ConceptoFacturableRepository conceptoFacturableRepository;
    private final ContratoElectricidadRepository contratoElectricidadRepository;
    private final EntityManager entityManager;

    // Servicios de otros módulos
    private final LecturaService lecturaService;
    private final PrecioConceptoService precioConceptoService;

    // Inyección por constructor (correcta)
    public FacturaServiceImpl(FacturaRepository facturaRepository,
                              PuntoVentaRepository puntoVentaRepository,
                              ComprobanteTipoRepository comprobanteTipoRepository,
                              ConceptoFacturableRepository conceptoFacturableRepository,
                              ContratoElectricidadRepository contratoElectricidadRepository,
                              LecturaService lecturaService,
                              PrecioConceptoService precioConceptoService,
                              EntityManager entityManager) {
        this.facturaRepository = facturaRepository;
        this.puntoVentaRepository = puntoVentaRepository;
        this.comprobanteTipoRepository = comprobanteTipoRepository;
        this.conceptoFacturableRepository = conceptoFacturableRepository;
        this.contratoElectricidadRepository = contratoElectricidadRepository;
        this.lecturaService = lecturaService;
        this.precioConceptoService = precioConceptoService;
        this.entityManager = entityManager;
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
        log.debug("Tipo de Comprobante determinado: {}", tipoComprobante.getCodigoAfip()); // Lombok generará getCodigoAfip()

        // --- 2. Obtener Contrato, Medidor y Categoría Tarifaria ---
        ContratoElectricidad contratoElec = contratoElectricidadRepository
                .findBySuministroAndEstado(suministro, ContratoServicio.EstadoContrato.ACTIVO)
                .orElseThrow(() -> new Exception("No se encontró un contrato de electricidad ACTIVO para el suministro " + suministro.getNis()));

        Medidor medidor = contratoElec.getMedidor();
        CategoriaTarifaria categoria = contratoElec.getCategoriaTarifaria();
        log.debug("Contrato activo encontrado. ID: {}. Medidor: {}, Categoría: {}", contratoElec.getId(), medidor.getNumero(), categoria.getCodigo());


        // --- 3. Obtener Lecturas y Calcular Consumo ---
        Optional<Lectura> optLecturaInicial = lecturaService.buscarUltimaLecturaAntesDe(contratoElec.getId(), fechaDesde);
        Optional<Lectura> optLecturaFinal = lecturaService.buscarPrimeraLecturaDesde(contratoElec.getId(), fechaHasta);

        if (optLecturaInicial.isEmpty() || optLecturaFinal.isEmpty()) {
            log.error("No se encontraron lecturas inicial/final para el período {} - {} en contrato {}", fechaDesde, fechaHasta, contratoElec.getId());
            throw new Exception("No se encontraron lecturas para definir el período de consumo.");
        }

        Lectura lecturaInicial = optLecturaInicial.get();
        Lectura lecturaFinal = optLecturaFinal.get();

        if (!lecturaFinal.getFechaPeriodo().isAfter(lecturaInicial.getFechaPeriodo()) ||
                lecturaFinal.getEstadoActual().compareTo(lecturaInicial.getEstadoActual()) < 0) {
            log.error("Lecturas inconsistentes: Inicial ({}, {}) vs Final ({}, {})",
                    lecturaInicial.getFechaPeriodo(), lecturaInicial.getEstadoActual(),
                    lecturaFinal.getFechaPeriodo(), lecturaFinal.getEstadoActual());
            throw new Exception("Lecturas de inicio/fin de período inconsistentes.");
        }

        BigDecimal consumoCalculado = (lecturaFinal.getEstadoActual().subtract(lecturaInicial.getEstadoActual()))
                .multiply(medidor.getConstanteMultiplicacion());

        log.info("Consumo calculado: {} kWh ( ({} - {}) * {} )", consumoCalculado.setScale(2, RoundingMode.HALF_UP),
                lecturaFinal.getEstadoActual(), lecturaInicial.getEstadoActual(), medidor.getConstanteMultiplicacion());


        // --- 4. Obtener Precios Vigentes y Calcular Items ---
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
                .orElseThrow(() -> new Exception("No se encontró precio vigente para 'ELEC_KWH' en categoría " + categoria.getCodigo()));
        BigDecimal importeEnergia = consumoCalculado.multiply(precioEnergia).setScale(2, RoundingMode.HALF_UP);

        FacturaDetalle detalleEnergia = new FacturaDetalle();
        detalleEnergia.setConceptoFacturable(conceptoEnergia); // Lombok generará setConceptoFacturable()
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
                .orElseThrow(() -> new Exception("No se encontró precio vigente para 'CARGO_FIJO' en categoría " + categoria.getCodigo()));

        FacturaDetalle detalleCargoFijo = new FacturaDetalle();
        detalleCargoFijo.setConceptoFacturable(conceptoCargoFijo); // Lombok generará setConceptoFacturable()
        detalleCargoFijo.setDescripcion(conceptoCargoFijo.getDescripcion());
        detalleCargoFijo.setCantidad(BigDecimal.ONE);
        detalleCargoFijo.setPrecioUnitario(precioCargoFijo);
        detalleCargoFijo.setImporteNeto(precioCargoFijo);
        detalles.add(detalleCargoFijo);
        subtotalNeto = subtotalNeto.add(precioCargoFijo);

        // --- 5. Calcular Impuestos Globales (IVA, etc.) ---
        BigDecimal totalImpuestos = BigDecimal.ZERO;
        BigDecimal alicuotaIvaGeneral = new BigDecimal("0.21");
        if (socio.getCondicionIVA() == Socio.CondicionIVA.RESPONSABLE_INSCRIPTO) {
            totalImpuestos = subtotalNeto.multiply(alicuotaIvaGeneral).setScale(2, RoundingMode.HALF_UP);
        } else {
            totalImpuestos = subtotalNeto.multiply(alicuotaIvaGeneral).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal totalFactura = subtotalNeto.add(totalImpuestos);


        // --- 6. Crear Objeto Factura ---
        Factura factura = new Factura();
        factura.setSocio(socio); // Lombok generará setSocio()
        factura.setSuministro(suministro);
        factura.setPuntoVenta(puntoVenta);
        factura.setComprobanteTipo(tipoComprobante);
        factura.setFechaEmision(LocalDate.now());
        factura.setPeriodoDesde(lecturaInicial.getFechaPeriodo());
        factura.setPeriodoHasta(lecturaFinal.getFechaPeriodo());
        factura.setFechaVencimiento(fechaVencimiento);
        factura.setImporteNeto(subtotalNeto);
        factura.setImporteIVA(totalImpuestos);
        factura.setImporteTotal(totalFactura);
        factura.setEstado("GENERADA");
        factura.setLecturaFinal(lecturaFinal); // Añadido para el Batch

        for(FacturaDetalle detalle : detalles) {
            factura.addDetalle(detalle);
        }

        log.info("Objeto Factura PRE-generado para Suministro NIS {}. Total: {}", suministro.getNis(), factura.getImporteTotal()); // Lombok generará getImporteTotal()

        return factura;
    }

    // --- MÉTODO DEL BATCH IMPLEMENTADO ---
    @Override
    @Transactional(readOnly = true)
    public Factura generarFacturaDesdeLectura(Lectura lecturaFinal,
                                              LocalDate fechaVencimiento,
                                              PuntoVenta puntoVenta) throws Exception {
        log.debug("Generando factura desde Lectura ID: {}", lecturaFinal.getId());

        ContratoElectricidad contratoElec = lecturaFinal.getContratoElectricidad();
        if (contratoElec == null) {
            throw new Exception("La Lectura ID " + lecturaFinal.getId() + " no tiene un contrato asociado.");
        }
        Suministro suministro = contratoElec.getSuministro();
        if (suministro == null) {
            throw new Exception("El Contrato ID " + contratoElec.getId() + " no tiene un suministro asociado.");
        }
        Socio socio = suministro.getSocio();
        if (socio == null) {
            throw new Exception("El Suministro ID " + suministro.getId() + " no tiene un socio asociado.");
        }
        Medidor medidor = contratoElec.getMedidor();
        CategoriaTarifaria categoria = contratoElec.getCategoriaTarifaria();

        ComprobanteTipo tipoComprobante = determinarTipoComprobante(socio.getCondicionIVA());

        Optional<Lectura> optLecturaInicial = lecturaService.buscarUltimaLecturaAntesDe(contratoElec.getId(), lecturaFinal.getFechaPeriodo());
        Lectura lecturaInicial = optLecturaInicial.orElse(null);

        // --- CORRECCIÓN LÓGICA: Usar el consumo pre-calculado de la Lectura ---
        // El consumo ya fue calculado (estadoActual - estadoAnterior) en LecturaServiceImpl
        BigDecimal consumoCalculado = lecturaFinal.getConsumoKwh()
                .multiply(medidor.getConstanteMultiplicacion());

        log.info("Consumo (pre-calculado en Lectura): {} kWh. Multiplicado por constante {}: {} kWh",
                lecturaFinal.getConsumoKwh(), medidor.getConstanteMultiplicacion(), consumoCalculado);

        LocalDate fechaReferenciaPrecios = lecturaFinal.getFechaPeriodo();
        List<PrecioConcepto> preciosVigentes = precioConceptoService.obtenerPreciosVigentes(categoria.getId(), fechaReferenciaPrecios);

        List<FacturaDetalle> detalles = new ArrayList<>();
        BigDecimal subtotalNeto = BigDecimal.ZERO;

        // Item: Consumo Eléctrico
        ConceptoFacturable conceptoEnergia = conceptoFacturableRepository.findByCodigo("ELEC_KWH")
                .orElseThrow(() -> new RuntimeException("Concepto 'ELEC_KWH' no encontrado."));
        BigDecimal precioEnergia = preciosVigentes.stream()
                .filter(p -> p.getConceptoFacturable().equals(conceptoEnergia))
                .map(PrecioConcepto::getPrecioUnitario)
                .findFirst()
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

        // Item: Cargo Fijo
        ConceptoFacturable conceptoCargoFijo = conceptoFacturableRepository.findByCodigo("CARGO_FIJO")
                .orElseThrow(() -> new RuntimeException("Concepto 'CARGO_FIJO' no encontrado."));
        BigDecimal precioCargoFijo = preciosVigentes.stream()
                .filter(p -> p.getConceptoFacturable().equals(conceptoCargoFijo))
                .map(PrecioConcepto::getPrecioUnitario)
                .findFirst()
                .orElseThrow(() -> new Exception("No se encontró precio vigente para 'CARGO_FIJO' en categoría " + categoria.getCodigo()));
        FacturaDetalle detalleCargoFijo = new FacturaDetalle();
        detalleCargoFijo.setConceptoFacturable(conceptoCargoFijo);
        detalleCargoFijo.setDescripcion(conceptoCargoFijo.getDescripcion());
        detalleCargoFijo.setCantidad(BigDecimal.ONE);
        detalleCargoFijo.setPrecioUnitario(precioCargoFijo);
        detalleCargoFijo.setImporteNeto(precioCargoFijo);
        detalles.add(detalleCargoFijo);
        subtotalNeto = subtotalNeto.add(precioCargoFijo);

        // Impuestos
        BigDecimal totalImpuestos = BigDecimal.ZERO;
        BigDecimal alicuotaIvaGeneral = new BigDecimal("0.21");
        if (socio.getCondicionIVA() == Socio.CondicionIVA.RESPONSABLE_INSCRIPTO) {
            totalImpuestos = subtotalNeto.multiply(alicuotaIvaGeneral).setScale(2, RoundingMode.HALF_UP);
        } else {
            totalImpuestos = subtotalNeto.multiply(alicuotaIvaGeneral).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal totalFactura = subtotalNeto.add(totalImpuestos);

        // Crear Factura
        Factura factura = new Factura();
        factura.setSocio(socio);
        factura.setSuministro(suministro);
        factura.setPuntoVenta(puntoVenta);
        factura.setComprobanteTipo(tipoComprobante);
        factura.setFechaEmision(LocalDate.now());
        factura.setPeriodoDesde(lecturaInicial != null ? lecturaInicial.getFechaPeriodo() : lecturaFinal.getFechaPeriodo().minusMonths(1)); // Asume período anterior si es la primera
        factura.setPeriodoHasta(lecturaFinal.getFechaPeriodo());
        factura.setFechaVencimiento(fechaVencimiento);
        factura.setImporteNeto(subtotalNeto);
        factura.setImporteIVA(totalImpuestos);
        factura.setImporteTotal(totalFactura);
        factura.setEstado("GENERADA");
        factura.setLecturaFinal(lecturaFinal); // Campo @Transient para el Writer

        for(FacturaDetalle detalle : detalles) {
            factura.addDetalle(detalle);
        }
        log.info("Objeto Factura PRE-generado (desde Batch) para Suministro NIS {}. Total: {}", suministro.getNis(), factura.getImporteTotal());
        return factura;
    }
    // --- FIN MÉTODO BATCH ---


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public long obtenerProximoNumeroComprobante(PuntoVenta puntoVenta, ComprobanteTipo comprobanteTipo) {
        log.debug("Obteniendo próximo número para PV {} y Tipo {}", puntoVenta.getNumero(), comprobanteTipo.getCodigoAfip()); // Lombok generará getNumero y getCodigoAfip

        PuntoVenta pvBloqueado = entityManager.find(PuntoVenta.class, puntoVenta.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (pvBloqueado == null) {
            throw new RuntimeException("Punto de Venta no encontrado o bloqueo fallido: ID " + puntoVenta.getId());
        }

        Optional<Factura> ultimaFacturaOpt = facturaRepository.findTopByPuntoVentaAndComprobanteTipoOrderByNumeroComprobanteDesc(
                pvBloqueado, comprobanteTipo);

        long proximoNumero = ultimaFacturaOpt.map(factura -> factura.getNumeroComprobante() + 1).orElse(1L); // Lombok generará getNumeroComprobante

        log.info("Próximo número determinado para PV {}: {}", pvBloqueado.getNumero(), proximoNumero);
        return proximoNumero;
    }


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Factura guardarFacturaYActualizarPuntoVenta(Factura factura, PuntoVenta puntoVenta) {
        log.info("Intentando guardar factura y actualizar PV {}", puntoVenta.getNumero()); // Lombok generará getNumero

        PuntoVenta pvBloqueado = entityManager.find(PuntoVenta.class, puntoVenta.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (pvBloqueado == null) {
            throw new RuntimeException("Punto de Venta no encontrado o bloqueo fallido al guardar: ID " + puntoVenta.getId());
        }

        long numeroAsignar = obtenerProximoNumeroComprobante(pvBloqueado, factura.getComprobanteTipo()); // Lombok generará getComprobanteTipo
        factura.setNumeroComprobante(numeroAsignar); // Lombok generará setNumeroComprobante
        log.debug("Número {} asignado a la factura", numeroAsignar);

        factura.setEstado("EMITIDA_PENDIENTE"); // Lombok generará setEstado

        Factura facturaGuardada = facturaRepository.save(factura);
        log.info("Factura ID {} guardada con Número {}", facturaGuardada.getId(), facturaGuardada.getNumeroComprobante()); // Lombok generará getId y getNumeroComprobante

        pvBloqueado.setUltimoNumeroEmitido(numeroAsignar); // Lombok generará setUltimoNumeroEmitido
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

