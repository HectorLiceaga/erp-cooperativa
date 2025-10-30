package com.cooperativa.erp.facturacion.controller;

import com.cooperativa.erp.facturacion.dto.FacturacionJobRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/facturacion/batch")
public class FacturacionBatchController {

    private static final Logger log = LoggerFactory.getLogger(FacturacionBatchController.class);

    private final JobLauncher jobLauncher;
    private final Job facturacionPeriodoJob;

    public FacturacionBatchController(JobLauncher jobLauncher,
                                      @Qualifier("facturacionPeriodoJob") Job facturacionPeriodoJob) {
        this.jobLauncher = jobLauncher;
        this.facturacionPeriodoJob = facturacionPeriodoJob;
    }

    /**
     * Endpoint para lanzar el Job de Facturación Masiva.
     * Esto se ejecuta de forma asíncrona.
     */
    @PostMapping("/lanzar")
    // TODO: Añadir @PreAuthorize("hasRole('ROLE_FACTURACION')") cuando Spring Security (Fase 1) esté listo
    public ResponseEntity<String> lanzarJobFacturacion(@Valid @RequestBody FacturacionJobRequest request) {

        log.info("Recibida solicitud para lanzar Job de Facturación: {}", request);

        try {
            // Convertimos los parámetros del DTO a JobParameters (Spring Batch)
            // Añadimos 'runTime' para asegurar que cada ejecución sea única
            JobParameters jobParameters = new JobParametersBuilder()
                    .addDate("runTime", new Date()) // Parámetro de desambiguación
                    .addDate("periodo", Date.from(request.getPeriodo().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .addDate("fechaVencimiento", Date.from(request.getFechaVencimiento().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .addLong("puntoVentaId", request.getPuntoVentaId().longValue()) // Lo pasamos como Long al Job
                    .toJobParameters();

            // Lanzamos el Job (asíncrono por defecto si se configura un TaskExecutor)
            var jobExecution = jobLauncher.run(facturacionPeriodoJob, jobParameters);

            String responseMessage = "Job de facturación lanzado. ID de Ejecución: " + jobExecution.getId();
            log.info(responseMessage);

            return ResponseEntity.ok(responseMessage);

        } catch (Exception e) {
            String errorMessage = "Error al lanzar el job de facturación: " + e.getMessage();
            log.error(errorMessage, e);
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }
}
