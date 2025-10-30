package com.cooperativa.erp.facturacion.batch;

import com.cooperativa.erp.electricidad.entity.Lectura;
import com.cooperativa.erp.electricidad.repository.LecturaRepository;
import com.cooperativa.erp.facturacion.entity.Factura;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.Map;

@Configuration
public class FacturacionJobConfig {

    @Bean
    public Job facturacionPeriodoJob(JobRepository jobRepository, Step facturacionStep) {
        return new JobBuilder("facturacionPeriodoJob", jobRepository)
                .flow(facturacionStep)
                .end()
                .build();
    }

    @Bean
    public Step facturacionStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                RepositoryItemReader<Lectura> facturacionReader,
                                FacturacionProcessor facturacionProcessor,
                                FacturacionWriter facturacionWriter,
                                TaskExecutor taskExecutor) {
        return new StepBuilder("facturacionStep", jobRepository)
                .<Lectura, Factura>chunk(100, transactionManager) // Procesar en lotes de 100
                .reader(facturacionReader)
                .processor(facturacionProcessor)
                .writer(facturacionWriter)
                .taskExecutor(taskExecutor) // Habilitar procesamiento paralelo
                .build();
    }

    @Bean
    public RepositoryItemReader<Lectura> facturacionReader(
            LecturaRepository lecturaRepository,
            @Value("#{jobParameters['periodo']}") String periodoStr) {

        LocalDate periodo = LocalDate.parse(periodoStr);

        return new RepositoryItemReaderBuilder<Lectura>()
                .name("facturacionReader")
                .repository(lecturaRepository)
                .methodName("findByFechaPeriodoAndFacturada")
                .arguments(periodo, false) // Busca lecturas del período que NO estén facturadas
                .pageSize(100) // Coincide con el chunk size
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public FacturacionProcessor facturacionProcessor(
            @Value("#{jobParameters['vencimiento']}") String vencimientoStr,
            // --- CORRECCIÓN DE TIPO ---
            // Cambiado de Long a Integer para que coincida con el ID de PuntoVenta
            @Value("#{jobParameters['puntoVentaId']}") Integer puntoVentaId) {

        return new FacturacionProcessor(vencimientoStr, puntoVentaId);
    }

    @Bean
    public FacturacionWriter facturacionWriter() {
        return new FacturacionWriter();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        // Configuración para procesamiento paralelo del Step
        // Usará 4 hilos para procesar el lote
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100 * 4); // Capacidad de cola (chunkSize * corePoolSize)
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }
}

