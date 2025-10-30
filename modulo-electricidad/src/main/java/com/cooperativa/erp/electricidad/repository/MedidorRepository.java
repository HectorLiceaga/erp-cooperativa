package com.cooperativa.erp.electricidad.repository;

import com.cooperativa.erp.electricidad.entity.Medidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Necesario para Optional

@Repository
public interface MedidorRepository extends JpaRepository<Medidor, Long> {

    /**
     * Busca un medidor por su número/serie único.
     * Spring Data JPA infiere la consulta: "SELECT m FROM Medidor m WHERE m.numero = ?1"
     * @param numero El número/serie del medidor a buscar.
     * @return Un Optional conteniendo el medidor si se encuentra, o vacío si no.
     */
    Optional<Medidor> findByNumero(String numero);

}

