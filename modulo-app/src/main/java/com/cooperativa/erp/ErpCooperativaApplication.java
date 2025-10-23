package com.cooperativa.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal para el ERP de la Cooperativa.
 * * Esta clase, al estar en el 'modulo-app', tiene visibilidad
 * (gracias a las dependencias de Maven) de todos los demás módulos.
 * * Spring Boot escaneará automáticamente los componentes (@Service, @Repository, @Entity)
 * de 'modulo-core' y de cualquier otro módulo que agreguemos.
 */
@SpringBootApplication
public class ErpCooperativaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpCooperativaApplication.class, args);
    }

}
