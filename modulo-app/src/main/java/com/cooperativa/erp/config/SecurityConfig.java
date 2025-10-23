package com.cooperativa.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Indica a Spring que esta clase contiene configuración de seguridad
@Configuration
// Habilita la seguridad web de Spring Security
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Bean para el codificador de contraseñas.
     * Usamos BCrypt, que es el estándar actual.
     * Cada vez que necesitemos codificar una contraseña (ej. al crear un usuario)
     * o verificar una (al hacer login), Spring usará esta instancia.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean para el AuthenticationManager.
     * Es necesario para procesar las solicitudes de autenticación.
     * Lo obtenemos a partir de la configuración de autenticación estándar de Spring.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configuración principal de la cadena de filtros de seguridad.
     * Aquí definimos qué peticiones requieren autenticación, qué tipo de login usamos, etc.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitamos CSRF por ahora. Si tuviéramos un frontend con formularios, lo habilitaríamos.
                // Para una API REST, a menudo se deshabilita si se usa autenticación basada en tokens (como JWT).
                .csrf(csrf -> csrf.disable())

                // Configuración de autorización de peticiones HTTP
                .authorizeHttpRequests(authz -> authz
                        // Cualquier petición (anyRequest) debe estar autenticada (authenticated)
                        .anyRequest().authenticated()
                )

                // Configuración del login por formulario (el que viene por defecto con Spring Security)
                .formLogin(formLogin -> formLogin
                        // Permite a cualquiera acceder a la página de login (si no, ¿cómo se loguearían?)
                        .loginPage("/login").permitAll()
                        // A dónde redirigir después de un login exitoso
                        .defaultSuccessUrl("/", true)
                )

                // Configuración del logout
                .logout(logout -> logout
                        // Permite a cualquiera acceder a la URL de logout
                        .logoutUrl("/logout").permitAll()
                        // A dónde redirigir después de un logout exitoso
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }

    /*
     * NOTA: Falta configurar el UserDetailsService.
     * Spring Boot es lo suficientemente inteligente como para detectar nuestro
     * UserDetailsServiceImpl (porque implementa UserDetailsService y está anotado con @Service)
     * y lo usará automáticamente junto con el PasswordEncoder que definimos.
     * No necesitamos enlazarlo explícitamente aquí en la configuración básica.
     */
}
