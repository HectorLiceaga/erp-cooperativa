package com.cooperativa.erp.electricidad.repository;

import com.cooperativa.erp.electricidad.entity.ConceptoFacturable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConceptoFacturableRepository extends JpaRepository<ConceptoFacturable, Long> {
    Optional<ConceptoFacturable> findByCodigo(String codigo);
}

