package com.cooperativa.erp.facturacion.repository;

import com.cooperativa.erp.facturacion.entity.ComprobanteTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteTipoRepository extends JpaRepository<ComprobanteTipo, Integer> {
    Optional<ComprobanteTipo> findByCodigoAfip(String codigoAfip);
    Optional<ComprobanteTipo> findByDescripcionIgnoreCase(String descripcion);
}

