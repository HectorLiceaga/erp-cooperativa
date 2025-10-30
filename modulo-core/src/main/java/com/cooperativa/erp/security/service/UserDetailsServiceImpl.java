package com.cooperativa.erp.security.service;

import com.cooperativa.erp.security.entity.Rol;
import com.cooperativa.erp.security.entity.Usuario;
import com.cooperativa.erp.security.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio que implementa UserDetailsService.
 * Spring Security lo usa para cargar los detalles de un usuario por su username.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    // Inyección por constructor (mejor práctica)
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true) // Es una operación de solo lectura
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscamos el usuario en nuestro repositorio
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            throw new UsernameNotFoundException("No se encontró el usuario: " + username);
        }
        Usuario usuario = usuarioOpt.get();

        // 2. Convertimos nuestros Roles a GrantedAuthority (lo que Spring Security entiende)
        Set<Rol> roles = usuario.getRoles();
        List<GrantedAuthority> authorities = roles.stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre())) // Ej. "ROLE_ADMIN"
                .collect(Collectors.toList());

        // 3. Creamos y devolvemos el objeto UserDetails que Spring entiende
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.isActivo(), // <--- CORREGIDO: Usamos el getter de Lombok para 'activo'
                true, // accountNonExpired (podríamos añadir campos para esto en Usuario si fuera necesario)
                true, // credentialsNonExpired (podríamos añadir campos para esto en Usuario)
                true, // accountNonLocked (podríamos añadir campos para esto en Usuario)
                authorities
        );
    }
}

