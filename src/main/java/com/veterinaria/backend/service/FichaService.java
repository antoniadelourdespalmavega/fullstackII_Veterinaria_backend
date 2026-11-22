package com.veterinaria.backend.service;

import com.veterinaria.backend.exception.ApiException;
import com.veterinaria.backend.model.Ficha;
import com.veterinaria.backend.model.Usuario;
import com.veterinaria.backend.repository.FichaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

// Lógica de negocio de las fichas clínicas de los pacientes.
@Service
@Transactional
public class FichaService {

    private final FichaRepository repo;

    public FichaService(FichaRepository repo) {
        this.repo = repo;
    }

    // El personal ve todas las fichas; el cliente solo las de sus mascotas (por su correo)
    @Transactional(readOnly = true)
    public List<Ficha> listar(String correo, String rol) {
        if (Usuario.CLIENTE.equals(rol)) return repo.findByCorreoIgnoreCaseOrderByCreadoEnDesc(correo);
        return repo.findAllByOrderByCreadoEnDesc();
    }

    @Transactional(readOnly = true)
    public Ficha obtener(String id, String correo, String rol) {
        Ficha f = buscar(id);
        if (Usuario.CLIENTE.equals(rol) && (f.getCorreo() == null || !f.getCorreo().equalsIgnoreCase(correo))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta ficha.");
        }
        return f;
    }

    // Crea una ficha nueva (no pagada por defecto). Los ids nuevos parten desde PAC-101.
    public Ficha crear(Ficha datos) {
        validar(datos);
        datos.setId(nuevoId());
        datos.setPagado(false);
        datos.setDetallePago(null);
        datos.setCreadoEn(Instant.now());
        return repo.save(datos);
    }

    // Actualiza la ficha completa (diagnóstico, tratamiento, estado, pago...)
    public Ficha actualizar(String id, Ficha datos) {
        Ficha actual = buscar(id);
        validar(datos);
        datos.setId(id);
        datos.setCreadoEn(actual.getCreadoEn()); // la fecha de creación no cambia
        return repo.save(datos);
    }

    public void eliminar(String id) {
        repo.delete(buscar(id));
    }

    private Ficha buscar(String id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se encontró la ficha."));
    }

    private void validar(Ficha f) {
        if (f.getMascota() == null || f.getMascota().isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "El nombre de la mascota es obligatorio.");
        if (f.getDueno() == null || f.getDueno().isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "El nombre del dueño es obligatorio.");
        if (f.getEspecie() == null || f.getEspecie().isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "La especie es obligatoria.");
        if (f.getRaza() == null || f.getRaza().isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "La raza es obligatoria.");
        Validaciones.correo(f.getCorreo());
        Validaciones.peso(f.getPeso());
        // El estado solo puede ser uno de los dos que usa el sistema
        if (!Ficha.EN_TRATAMIENTO.equals(f.getEstado()) && !Ficha.FINALIZADO.equals(f.getEstado())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El estado debe ser \"" + Ficha.EN_TRATAMIENTO + "\" o \"" + Ficha.FINALIZADO + "\".");
        }
    }

    // Busca el número más grande que exista (mínimo 100) y le suma 1: PAC-101, PAC-102...
    private String nuevoId() {
        int max = repo.findAll().stream()
                .map(f -> f.getId().replaceAll("\\D", ""))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .max().orElse(100);
        return "PAC-" + String.format("%03d", Math.max(max, 100) + 1);
    }
}
