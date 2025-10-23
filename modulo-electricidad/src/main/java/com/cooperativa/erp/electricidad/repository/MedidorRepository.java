package com.cooperativa.erp.electricidad.repository;

import com.cooperativa.erp.electricidad.entity.Medidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedidorRepository extends JpaRepository<Medidor, Long> {

    Optional<Medidor> findByNumeroSerie(String numeroSerie);

    // Podríamos necesitar buscar por suministro
    Optional<Medidor> findBySuministroId(Long suministroId);
}

