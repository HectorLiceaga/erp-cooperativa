package com.cooperativa.erp.facturacion.repository;

import com.cooperativa.erp.facturacion.entity.PuntoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PuntoVentaRepository extends JpaRepository<PuntoVenta, Integer> {
    Optional<PuntoVenta> findByNumero(Integer numero);
}

