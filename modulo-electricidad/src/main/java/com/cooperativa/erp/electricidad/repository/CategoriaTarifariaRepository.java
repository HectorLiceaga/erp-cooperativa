package com.cooperativa.erp.electricidad.repository;

import com.cooperativa.erp.electricidad.entity.CategoriaTarifaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaTarifariaRepository extends JpaRepository<CategoriaTarifaria, Long> {
    Optional<CategoriaTarifaria> findByCodigo(String codigo);
}

