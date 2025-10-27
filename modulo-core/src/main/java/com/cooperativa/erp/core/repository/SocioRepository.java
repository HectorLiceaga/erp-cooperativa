package com.cooperativa.erp.core.repository;

import com.cooperativa.erp.core.entity.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {

    Optional<Socio> findByCuitCuil(String cuitCuil);

}

