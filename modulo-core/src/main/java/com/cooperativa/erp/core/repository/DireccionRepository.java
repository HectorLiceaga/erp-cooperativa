package com.cooperativa.erp.core.repository; // Corregido

import com.cooperativa.erp.core.entity.Direccion; // Corregido
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    // Métodos de búsqueda personalizados si fueran necesarios en el futuro
}
