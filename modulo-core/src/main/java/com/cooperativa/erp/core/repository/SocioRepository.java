package com.cooperativa.erp.core.repository;

import com.cooperativa.erp.core.entity.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {

    /**
     * Busca un Socio por su número de CUIT/CUIL.
     * El nombre del método DEBE coincidir con el nombre de la propiedad en la entidad Socio.
     *
     * @param cuit El CUIT/CUIL a buscar.
     * @return Un Optional que contiene el Socio si se encuentra, o vacío si no.
     */
    Optional<Socio> findByCuit(String cuit); // <-- CORREGIDO: findByCuit en lugar de findByCuitCuil
}

