package com.cooperativa.erp.security.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cooperativa.erp.security.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Método CRÍTICO para Spring Security.
     * Lo usaremos para buscar un usuario por su username durante el login.
     */
    Optional<Usuario> findByUsername(String username);

    // Métodos de utilidad que podríamos necesitar
    boolean existsByUsername(String username);
}
