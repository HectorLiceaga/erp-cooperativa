package com.cooperativa.erp.security.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cooperativa.erp.security.entity.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    // Método para buscar un rol por su nombre
    Optional<Rol> findByNombre(String nombre);
}
