package com.cooperativa.erp.facturacion.service;

// --- Imports de Contabilidad (NUEVOS) ---
import com.cooperativa.erp.contabilidad.dto.AsientoDTO;
import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;
import com.cooperativa.erp.contabilidad.service.AsientoService;
import com.cooperativa.erp.contabilidad.service.ParametroContableService;
// --- Fin Imports Contabilidad ---

import com.cooperativa.erp.core.entity.ContratoServicio;
import com.cooperativa.erp.core.entity.Socio;
import com.cooperativa.erp.core.entity.Suministro;
import com.cooperativa.erp.electricidad.entity.*;
import com.cooperativa.erp.electricidad.repository.ConceptoFacturableRepository;
import com.cooperativa.erp.electricidad.repository.ContratoElectricidadRepository;
import com.cooperativa.erp.electricidad.service.LecturaService;
import com.cooperativa.erp.electricidad.service.PrecioConceptoService;
import com.cooperativa.erp.facturacion.entity.*;
import com.cooperativa.erp.facturacion.repository.ComprobanteTipoRepository;
import com.cooperativa.erp.facturacion.repository.FacturaRepository;
import com.cooperativa.erp.facturacion.repository.PuntoVentaRepository;
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

    // --- Repositorios y Servicios existentes ---
    private final FacturaRepository facturaRepository;
    private final PuntoVentaRepository puntoVentaRepository;
    private final ComprobanteTipoRepository comprobanteTipoRepository;
    private final ConceptoFacturableRepository conceptoFacturableRepository;
    private final ContratoElectricidadRepository contratoElectricidadRepository;
    private final EntityManager entityManager;
    private final LecturaService lecturaService;
    private final PrecioConceptoService precioConceptoService;
    private final CtaCteService ctaCteService;

    // --- SERVICIOS AÑADIDOS (FASE 5) ---
    private final AsientoService asientoService;
    private final ParametroContableService parametroContableService;

    // --- CONSTRUCTOR ACTUALIZADO ---
    public FacturaServiceImpl(FacturaRepository facturaRepository,
                              PuntoVentaRepository puntoVentaRepository,
                              ComprobanteTipoRepository comprobanteTipoRepository,
                              ConceptoFacturableRepository conceptoFacturableRepository,
                              ContratoElectricidadRepository contratoElectricidadRepository,
                              LecturaService lecturaService,
                              PrecioConceptoService precioConceptoService,
                              EntityManager entityManager,
                              CtaCteService ctaCteService,
                              // --- Inyección Fase 5 ---
                              AsientoService asientoService,
                              ParametroContableService parametroContableService) {
        this.facturaRepository = facturaRepository;
        this.puntoVentaRepository = puntoVentaRepository;
        this.comprobanteTipoRepository = comprobanteTipoRepository;
        this.conceptoFacturableRepository = conceptoFacturableRepository;
        this.contratoElectricidadRepository = contratoElectricidadRepository;
        this.lecturaService = lecturaService;
        this.precioConceptoService = precioConceptoService;
        this.entityManager = entityManager;
        this.ctaCteService = ctaCteService;
        // --- Asignación Fase 5 ---
        this.asientoService = asientoService;
        this.parametroContableService = parametroContableService;
    }

    // ... (El método generarFacturaPeriodo() no cambia) ...
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

        ComprobanteTipo tipoComprobante = determinarTipoComprobante(socio.getCondicionIVA());
        log.debug("Tipo de Comprobante determinado: {}", tipoComprobante.getCodigoAfip());

        ContratoElectricidad contratoElec = contratoElectricidadRepository
                .findBySuministroAndEstado(suministro, ContratoServicio.EstadoContrato.ACTIVO)
                .orElseThrow(() -> new Exception("No se encontró un contrato de electricidad ACTIVO para el suministro " + suministro.getNis()));

        Medidor medidor = contratoElec.getMedidor();
        CategoriaTarifaria categoria = contratoElec.getCategoriaTarifaria();
        log.debug("Contrato activo encontrado. ID: {}. Medidor: {}, Categoría: {}", contratoElec.getId(), medidor.getNumero(), categoria.getCodigo());


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


        List<PrecioConcepto> preciosVigentes = precioConceptoService.obtenerPreciosVigentes(categoria.getId(), fechaHasta);
        log.debug("Se encontraron {} precios vigentes para la categoría {} en fecha {}",
                preciosVigentes.size(), categoria.getCodigo(), fechaHasta);

        List<FacturaDetalle> detalles = new ArrayList<>();
        BigDecimal subtotalNeto = BigDecimal.ZERO;

        ConceptoFacturable conceptoEnergia = conceptoFacturableRepository.findByCodigo("ELEC_KWH")
                .orElseThrow(() -> new RuntimeException("Concepto 'ELEC_KWH' no encontrado en la base de datos."));
        BigDecimal precioEnergia = preciosVigentes.stream()
                .filter(p -> p.getConceptoFacturable().equals(conceptoEnergia))
                .map(PrecioConcepto::getPrecioUnitario)
                .findFirst()
                .orElseThrow(() -> new Exception("No se encontró precio vigente para 'ELEC_KWH' en categoría " + categoria.getCodigo()));
        BigDecimal importeEnergia = consumoCalculado.multiply(precioEnergia).setScale(2, RoundingMode.HALF_UP);

        FacturaDetalle detalleEnergia = new FacturaDetalle();
        detalleEnergia.setConceptoFacturable(conceptoEnergia);
        detalleEnergia.setDescripcion(String.format("%s (%s kWh @ %s)", conceptoEnergia.getDescripcion(), consumoCalculado, precioEnergia));
        detalleEnergia.setCantidad(consumoCalculado);
        detalleEnergia.setPrecioUnitario(precioEnergia);
        detalleEnergia.setImporteNeto(importeEnergia);
        detalles.add(detalleEnergia);
        subtotalNeto = subtotalNeto.add(importeEnergia);

        ConceptoFacturable conceptoCargoFijo = conceptoFacturableRepository.findByCodigo("CARGO_FIJO")
                .orElseThrow(() -> new RuntimeException("Concepto 'CARGO_FIJO' no encontrado en la base de datos."));
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

        BigDecimal totalImpuestos = BigDecimal.ZERO;
        BigDecimal alicuotaIvaGeneral = new BigDecimal("0.21");
        if (socio.getCondicionIVA() == Socio.CondicionIVA.RESPONSABLE_INSCRIPTO) {
            totalImpuestos = subtotalNeto.multiply(alicuotaIvaGeneral).setScale(2, RoundingMode.HALF_UP);
        } else {
            totalImpuestos = subtotalNeto.multiply(alicuotaIvaGeneral).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal totalFactura = subtotalNeto.add(totalImpuestos);

        Factura factura = new Factura();
        factura.setSocio(socio);
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
        factura.setLecturaFinal(lecturaFinal);

        for(FacturaDetalle detalle : detalles) {
            factura.addDetalle(detalle);
        }

        log.info("Objeto Factura PRE-generado para Suministro NIS {}. Total: {}", suministro.getNis(), factura.getImporteTotal());

        return factura;
    }

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

        BigDecimal consumoCalculado = lecturaFinal.getConsumoKwh()
                .multiply(medidor.getConstanteMultiplicacion());

        log.info("Consumo (pre-calculado en Lectura): {} kWh. Multiplicado por constante {}: {} kWh",
                lecturaFinal.getConsumoKwh(), medidor.getConstanteMultiplicacion(), consumoCalculado);

        LocalDate fechaReferenciaPrecios = lecturaFinal.getFechaPeriodo();
        List<PrecioConcepto> preciosVigentes = precioConceptoService.obtenerPreciosVigentes(categoria.getId(), fechaReferenciaPrecios);

        List<FacturaDetalle> detalles = new ArrayList<>();
        BigDecimal subtotalNeto = BigDecimal.ZERO;

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
        detalleEnergia.setDescripcion(String.format("%s (%s kWh @ %s)", conceptoEnergia.getDescripcion(), consumoCalculado, precioEnergia));
        detalleEnergia.setCantidad(consumoCalculado);
        detalleEnergia.setPrecioUnitario(precioEnergia);
        detalleEnergia.setImporteNeto(importeEnergia);
        detalles.add(detalleEnergia);
        subtotalNeto = subtotalNeto.add(importeEnergia);

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

        BigDecimal totalImpuestos = BigDecimal.ZERO;
        BigDecimal alicuotaIvaGeneral = new BigDecimal("0.21");
        if (socio.getCondicionIVA() == Socio.CondicionIVA.RESPONSABLE_INSCRIPTO) {
            totalImpuestos = subtotalNeto.multiply(alicuotaIvaGeneral).setScale(2, RoundingMode.HALF_UP);
        } else {
            totalImpuestos = subtotalNeto.multiply(alicuotaIvaGeneral).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal totalFactura = subtotalNeto.add(totalImpuestos);

        Factura factura = new Factura();
        factura.setSocio(socio);
        factura.setSuministro(suministro);
        factura.setPuntoVenta(puntoVenta);
        factura.setComprobanteTipo(tipoComprobante);
        factura.setFechaEmision(LocalDate.now());
        factura.setPeriodoDesde(lecturaInicial != null ? lecturaInicial.getFechaPeriodo() : lecturaFinal.getFechaPeriodo().minusMonths(1));
        factura.setPeriodoHasta(lecturaFinal.getFechaPeriodo());
        factura.setFechaVencimiento(fechaVencimiento);
        factura.setImporteNeto(subtotalNeto);
        factura.setImporteIVA(totalImpuestos);
        factura.setImporteTotal(totalFactura);
        factura.setEstado("GENERADA");
        factura.setLecturaFinal(lecturaFinal);

        for(FacturaDetalle detalle : detalles) {
            factura.addDetalle(detalle);
        }
        log.info("Objeto Factura PRE-generado (desde Batch) para Suministro NIS {}. Total: {}", suministro.getNis(), factura.getImporteTotal());
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
        log.info("Iniciando guardado transaccional de Factura para PV {}", puntoVenta.getNumero());

        // 1. Bloquear PV y Asignar Número
        PuntoVenta pvBloqueado = entityManager.find(PuntoVenta.class, puntoVenta.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (pvBloqueado == null) {
            throw new RuntimeException("Punto de Venta no encontrado o bloqueo fallido al guardar: ID " + puntoVenta.getId());
        }

        long numeroAsignar = obtenerProximoNumeroComprobante(pvBloqueado, factura.getComprobanteTipo());
        factura.setNumeroComprobante(numeroAsignar);
        factura.setEstado("EMITIDA_PENDIENTE"); // (Pendiente de AFIP/Cierre)
        log.debug("Número {} asignado a la factura", numeroAsignar);

        // 2. Guardar Factura
        Factura facturaGuardada = facturaRepository.save(factura);
        log.info("Factura ID {} guardada con Número {}", facturaGuardada.getId(), facturaGuardada.getNumeroComprobante());

        // 3. Actualizar Punto de Venta
        pvBloqueado.setUltimoNumeroEmitido(numeroAsignar);
        puntoVentaRepository.save(pvBloqueado);
        log.info("Último número actualizado en PV {} a {}", pvBloqueado.getNumero(), numeroAsignar);

        // 4. Registrar Movimiento en Cuenta Corriente (Fase 3)
        // (Llamada corregida en el paso anterior)
        ctaCteService.registrarDebePorFactura(facturaGuardada);
        log.info("Movimiento 'DEBE' registrado en CtaCte del Socio ID {}", factura.getSocio().getId());


        // --- 5. INTEGRACIÓN FASE 5: Generar Asiento Contable ---
        try {
            log.debug("Iniciando generación de asiento contable para Factura ID {}", facturaGuardada.getId());
            generarAsientoContableParaFactura(facturaGuardada);
        } catch (Exception e) {
            log.error("¡FALLO CRÍTICO! Error al generar asiento contable: {}. ¡Haciendo Rollback!", e.getMessage());
            throw new RuntimeException("Error al generar asiento contable (Rollback forzado): " + e.getMessage(), e);
        }

        return facturaGuardada;
    }

    /**
     * Método privado helper para la lógica de generación de asientos (FASE 5).
     *
     * --- ESTE MÉTODO ESTÁ AHORA CORREGIDO ---
     */
    private void generarAsientoContableParaFactura(Factura factura) throws RuntimeException {
        // 1. Obtener parámetros contables (preguntamos a modulo-contabilidad)
        PlanDeCuentas ctaClientes = parametroContableService.getCuentaByClave("CTA_CLIENTES_VENTA");
        PlanDeCuentas ctaVentaEnergia = parametroContableService.getCuentaByClave("CTA_VENTA_ENERGIA");
        // --- CORRECCIÓN 1: Faltaba definir ctaIvaDebito ---
        PlanDeCuentas ctaIvaDebito = parametroContableService.getCuentaByClave("CTA_IVA_DEBITO");
        // TODO: Mapear otros conceptos (Cargo Fijo, Alumbrado, etc.)

        // 2. Armar el Asiento DTO
        String leyendaAsiento = String.format("Factura %s %s-%08d | Socio: %s",
                factura.getComprobanteTipo().getLetra(),
                factura.getPuntoVenta().getNumero(),
                factura.getNumeroComprobante(),
                factura.getSocio().getCuit());

        // --- CORRECCIÓN 2: Faltaba instanciar el DTO ---
        AsientoDTO asientoDTO = new AsientoDTO();
        asientoDTO.setFecha(factura.getFechaEmision());
        asientoDTO.setDescripcion(leyendaAsiento);

        // --- CORRECCIÓN 3: Typo 'PlanDeCte' por 'asientoDTO' ---
        asientoDTO.setOrigen("modulo_facturacion");

        // 3. Crear Renglones (Debe y Haber)

        // Renglón 1: Clientes (Debe)
        asientoDTO.addDetalle(
                ctaClientes,
                "Debe por Venta",
                factura.getImporteTotal(), // Debe
                BigDecimal.ZERO            // Haber
        );

        // Renglón 2: Venta de Energía (Haber)
        // (Simplificación: asumimos que ImporteNeto es SOLO Venta de Energía)
        asientoDTO.addDetalle(
                ctaVentaEnergia,
                "Venta Neta de Energía",
                BigDecimal.ZERO,           // Debe
                factura.getImporteNeto()   // Haber
        );

        // Renglón 3: IVA Débito Fiscal (Haber)
        asientoDTO.addDetalle(
                ctaIvaDebito, // <-- Ahora está definida
                "IVA Débito Fiscal 21%",
                BigDecimal.ZERO,           // Debe
                factura.getImporteIVA()    // Haber
        );

        // 4. Llamar al servicio de contabilidad para registrar el asiento
        asientoService.registrarAsientoManual(asientoDTO); // <-- Ahora está definida
        log.info("Asiento contable registrado exitosamente para Factura ID {}", factura.getId());
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