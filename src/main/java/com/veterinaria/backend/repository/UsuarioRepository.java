package com.veterinaria.backend.repository;

import com.veterinaria.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Acceso a la tabla USUARIOS. Spring crea las consultas a partir del nombre del método.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    // Sirve para comprobar que el veterinario de una cita existe y tiene la cuenta activa
    boolean existsByNombreIgnoreCaseAndRolAndActivoTrue(String nombre, String rol);
}
