package com.cooperativa.erp.security.service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooperativa.erp.security.entity.Rol;
import com.cooperativa.erp.security.entity.Usuario;
import com.cooperativa.erp.security.repository.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // Es 'final' porque se asigna en el constructor y nunca cambia.
    private final UsuarioRepository usuarioRepository;

    /**
     * Inyección por Constructor (la mejor práctica).
     * Spring verá este constructor y automáticamente "inyectará"
     * una instancia de UsuarioRepository.
     */
    @Autowired
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Este es el método que Spring Security llama cuando un usuario intenta loguearse.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscamos el usuario en nuestra base de datos
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado con username: " + username));

        // 2. Convertimos los Roles (nuestra entidad) a GrantedAuthority (Spring Security)
        Collection<? extends GrantedAuthority> authorities = mapRolesToAuthorities(usuario.getRoles());

        // 3. Creamos y devolvemos el objeto UserDetails que Spring entiende
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }

    /**
     * Método helper para convertir nuestro Set<Rol> a una Collection de GrantedAuthority
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Rol> roles) {
        return roles.stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toList());
    }
}

