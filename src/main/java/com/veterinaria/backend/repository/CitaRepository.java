package com.veterinaria.backend.repository;

import com.veterinaria.backend.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Acceso a la tabla CITAS.
public interface CitaRepository extends JpaRepository<Cita, String> {
    List<Cita> findAllByOrderByCreadoEnDesc();

    List<Cita> findByCorreoIgnoreCaseOrderByCreadoEnDesc(String correo);

    List<Cita> findByFecha(String fecha);

    List<Cita> findByDoctorAndFechaAndEstado(String doctor, String fecha, String estado);
}
