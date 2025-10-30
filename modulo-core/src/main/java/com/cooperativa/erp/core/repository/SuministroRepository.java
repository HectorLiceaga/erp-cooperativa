package com.cooperativa.erp.core.repository;

import com.cooperativa.erp.core.entity.Socio;
import com.cooperativa.erp.core.entity.Suministro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface SuministroRepository extends JpaRepository<Suministro, Long> {

    // Método para buscar por el NIS (Número de Identificación de Suministro)
    Optional<Suministro> findByNis(String nis); // CORREGIDO: Antes era findByIdentificadorUnico

    // Método para encontrar todos los suministros de un socio
    List<Suministro> findBySocio(Socio socio);

    // Puedes añadir otros métodos de búsqueda si son necesarios
    // Ejemplo: buscar por dirección, por ruta, etc.
}

