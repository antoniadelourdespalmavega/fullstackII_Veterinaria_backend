package com.veterinaria.backend.repository;

import com.veterinaria.backend.model.Ficha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Acceso a la tabla FICHAS.
public interface FichaRepository extends JpaRepository<Ficha, String> {
    List<Ficha> findAllByOrderByCreadoEnDesc();

    List<Ficha> findByCorreoIgnoreCaseOrderByCreadoEnDesc(String correo);
}
