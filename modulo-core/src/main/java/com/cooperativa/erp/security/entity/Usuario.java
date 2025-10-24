package com.cooperativa.erp.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "seg_usuarios")
@Data // Genera getters, setters, toString, equals, hashCode
@NoArgsConstructor // Constructor sin argumentos (requerido por JPA)
@AllArgsConstructor // Constructor con todos los argumentos (útil para creación)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    @Size(min = 8) // Validacion basica
    private String password; // Almacenaremos el hash de BCrypt

    @Column(nullable = false, length = 150)
    private String nombreCompleto;

    @Column(nullable = false)
    private boolean activo = true; // Renombrado de 'enabled' a 'activo'

    /**
     * Relación Muchos-a-Muchos con Rol.
     * FetchType.EAGER: Cuando cargamos un Usuario, SIEMPRE queremos traer sus roles.
     * Es crucial para la seguridad.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "seg_usuarios_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    // Lombok genera los constructores, getters y setters automáticamente
    // ¡No necesitamos escribirlos manualmente!
}

