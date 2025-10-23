package com.cooperativa.erp.core.repository;

import com.cooperativa.erp.core.entity.Suministro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuministroRepository extends JpaRepository<Suministro, Long> {

    List<Suministro> findBySocioId(Long socioId);

    Optional<Suministro> findByIdentificadorUnico(String identificadorUnico);

}

