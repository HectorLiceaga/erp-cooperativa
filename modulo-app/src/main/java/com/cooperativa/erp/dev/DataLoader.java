package com.cooperativa.erp.dev;

import com.cooperativa.erp.security.entity.Rol;
import com.cooperativa.erp.security.entity.Usuario;
import com.cooperativa.erp.security.repository.RolRepository;
import com.cooperativa.erp.security.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

/**
 * Componente para cargar datos iniciales de desarrollo (ej. usuario admin).
 * Se activa solo cuando el perfil 'dev' está activo o no hay perfiles activos (por defecto).
 */
@Component
@Profile({"default", "dev"}) // Se ejecuta por defecto o con el perfil 'dev'
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional // Importante para manejar la sesión de JPA
    public void run(String... args) throws Exception {
        System.out.println("Cargando datos iniciales de desarrollo...");

        // 1. Crear Rol ADMIN si no existe
        Optional<Rol> rolAdminOpt = rolRepository.findByNombre("ROLE_ADMIN");
        Rol rolAdmin;
        if (rolAdminOpt.isEmpty()) {
            rolAdmin = new Rol("ROLE_ADMIN");
            rolRepository.save(rolAdmin);
            System.out.println("Rol 'ROLE_ADMIN' creado.");
        } else {
            rolAdmin = rolAdminOpt.get();
        }

        // 2. Crear Usuario admin si no existe
        Optional<Usuario> adminOpt = usuarioRepository.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            // ¡Importante! Codificar la contraseña
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setActivo(true);
            admin.setRoles(Collections.singleton(rolAdmin)); // Asignar el rol ADMIN

            usuarioRepository.save(admin);
            System.out.println("Usuario 'admin' creado con contraseña 'password' y rol 'ROLE_ADMIN'.");
        } else {
            System.out.println("Usuario 'admin' ya existe.");
        }

        System.out.println("Datos iniciales cargados.");
    }
}

