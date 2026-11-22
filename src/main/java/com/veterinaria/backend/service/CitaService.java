package com.veterinaria.backend.service;

import com.veterinaria.backend.exception.ApiException;
import com.veterinaria.backend.model.Cita;
import com.veterinaria.backend.model.Usuario;
import com.veterinaria.backend.repository.CitaRepository;
import com.veterinaria.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// Lógica de negocio de las citas (las mismas reglas que tenía el frontend, ahora en el backend):
// - Una solicitud del cliente nace "Pendiente" y sin veterinario.
// - Una cita de Recepción nace "Confirmada" y no puede chocar con otra del mismo veterinario.
// - No se agenda en el pasado ni en domingo.
// - Anular no borra: cambia el estado a "Cancelada".
@Service
@Transactional
public class CitaService {

    private final CitaRepository repo;
    private final UsuarioRepository usuarios;
    private final AtomicInteger contador = new AtomicInteger();

    public CitaService(CitaRepository repo, UsuarioRepository usuarios) {
        this.repo = repo;
        this.usuarios = usuarios;
    }

    // El personal ve todas las citas; el cliente solo las suyas (identificadas por su correo)
    @Transactional(readOnly = true)
    public List<Cita> listar(String correo, String rol) {
        if (Usuario.CLIENTE.equals(rol)) return repo.findByCorreoIgnoreCaseOrderByCreadoEnDesc(correo);
        return repo.findAllByOrderByCreadoEnDesc();
    }

    @Transactional(readOnly = true)
    public Cita obtener(String id, String correo, String rol) {
        Cita c = buscar(id);
        if (Usuario.CLIENTE.equals(rol) && !c.getCorreo().equalsIgnoreCase(correo)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta cita.");
        }
        return c;
    }

    // Horas ya reservadas en un día (sin datos personales). Las citas anuladas liberan el horario.
    @Transactional(readOnly = true)
    public List<String> horasOcupadas(String fecha) {
        return repo.findByFecha(fecha).stream()
                .filter(c -> !Cita.CANCELADA.equals(c.getEstado()))
                .map(c -> c.getHora().substring(0, Math.min(5, c.getHora().length())))
                .toList();
    }

    // Solicitud que hace un cliente desde la web
    public Cita solicitar(Cita datos) {
        exigir(datos.getDueno(), "El nombre del dueño es obligatorio.");
        exigir(datos.getCorreo(), "El correo es obligatorio.");
        exigir(datos.getMascota(), "El nombre de la mascota es obligatorio.");
        exigir(datos.getHora(), "La hora es obligatoria.");
        Validaciones.correo(datos.getCorreo());
        Validaciones.horaDeAtencion(datos.getHora());
        validarFecha(datos.getFecha());

        String hora = datos.getHora().substring(0, Math.min(5, datos.getHora().length()));
        if (horasOcupadas(datos.getFecha()).contains(hora)) {
            throw new ApiException(HttpStatus.CONFLICT, "¡HORA NO DISPONIBLE! El día " + datos.getFecha() + " a las " + hora + " ya está reservado. Elige otro horario.");
        }

        Cita c = new Cita();
        c.setIdCita(nuevoId());
        c.setFecha(datos.getFecha());
        c.setHora(datos.getHora());
        c.setDueno(datos.getDueno().trim());
        c.setTelefono("Sin registrar");
        c.setCorreo(datos.getCorreo().trim().toLowerCase());
        c.setMascota(datos.getMascota().trim());
        c.setEspecie("Por definir");
        c.setRaza("Por definir");
        c.setDoctor("Por asignar");
        c.setMotivo(datos.getMotivo());
        c.setEstado(Cita.PENDIENTE);
        c.setCreadoEn(Instant.now());
        return repo.save(c);
    }

    // Cita que crea Recepción cuando el cliente va en persona: nace Confirmada
    public Cita agendarPresencial(Cita datos) {
        exigir(datos.getDoctor(), "Seleccione un veterinario.");
        exigir(datos.getDueno(), "El nombre del dueño es obligatorio.");
        exigir(datos.getCorreo(), "El correo es obligatorio.");
        exigir(datos.getMascota(), "El nombre de la mascota es obligatorio.");
        exigir(datos.getHora(), "La hora es obligatoria.");
        Validaciones.correo(datos.getCorreo());
        Validaciones.telefono(datos.getTelefono());
        Validaciones.horaDeAtencion(datos.getHora());
        validarVeterinario(datos.getDoctor());
        validarFecha(datos.getFecha());
        validarChoque(datos.getDoctor(), datos.getFecha(), datos.getHora(), null);

        Cita c = new Cita();
        c.setIdCita(nuevoId());
        c.setFecha(datos.getFecha());
        c.setHora(datos.getHora());
        c.setDueno(datos.getDueno().trim());
        c.setTelefono(datos.getTelefono());
        c.setCorreo(datos.getCorreo().trim().toLowerCase());
        c.setMascota(datos.getMascota().trim());
        c.setEspecie(datos.getEspecie());
        c.setRaza(datos.getRaza());
        c.setDoctor(datos.getDoctor());
        c.setMotivo(datos.getMotivo());
        c.setEstado(Cita.CONFIRMADA);
        c.setCreadoEn(Instant.now());
        return repo.save(c);
    }

    // Sirve para dos cosas: confirmar una solicitud pendiente o reagendar una cita ya confirmada
    public Cita gestionar(String id, String doctor, String fecha, String hora, String modo) {
        Cita c = buscar(id);
        exigir(doctor, "Seleccione un veterinario.");
        exigir(hora, "Seleccione un bloque horario.");
        Validaciones.horaDeAtencion(hora);
        validarVeterinario(doctor);
        validarFecha(fecha);
        validarChoque(doctor, fecha, hora, id);

        // Si reagendo, guardo dónde estaba antes para mostrar el aviso "Reagendada"
        if ("reagendar".equals(modo)) c.setFechaAnterior(c.getFecha() + " " + c.getHora() + " con " + c.getDoctor());
        c.setDoctor(doctor);
        c.setFecha(fecha);
        c.setHora(hora);
        c.setEstado(Cita.CONFIRMADA);
        return repo.save(c);
    }

    // Anula o rechaza una cita: no se borra, queda "Cancelada"
    public Cita anular(String id) {
        Cita c = buscar(id);
        c.setEstado(Cita.CANCELADA);
        return repo.save(c);
    }

    // Eliminación definitiva (solo administrador)
    public void eliminar(String id) {
        repo.delete(buscar(id));
    }

    // ---------------------------------------------------------------- reglas
    private Cita buscar(String id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se encontró la cita."));
    }

    private void exigir(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, mensaje);
    }

    // El veterinario debe existir (un usuario con rol veterinario) y tener la cuenta activa
    private void validarVeterinario(String doctor) {
        if (!usuarios.existsByNombreIgnoreCaseAndRolAndActivoTrue(doctor.trim(), Usuario.VETERINARIO)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El veterinario \"" + doctor.trim() + "\" no existe o está desactivado.");
        }
    }

    // La fecha es obligatoria, no puede ser pasada y la clínica no atiende los domingos
    private void validarFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "Seleccione una fecha.");
        LocalDate dia;
        try {
            dia = LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha no tiene un formato válido (AAAA-MM-DD).");
        }
        if (dia.isBefore(LocalDate.now())) throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha no puede estar en el pasado.");
        if (dia.getDayOfWeek() == DayOfWeek.SUNDAY) throw new ApiException(HttpStatus.BAD_REQUEST, "La clínica no atiende los domingos (Lunes a Sábado).");
    }

    // El veterinario no puede tener otra cita CONFIRMADA el mismo día y bloque (se ignora la propia al reagendar)
    private void validarChoque(String doctor, String fecha, String hora, String idPropio) {
        boolean choque = repo.findByDoctorAndFechaAndEstado(doctor, fecha, Cita.CONFIRMADA).stream()
                .anyMatch(o -> !o.getIdCita().equals(idPropio) && o.getHora().equals(hora));
        if (choque) {
            throw new ApiException(HttpStatus.CONFLICT, doctor + " ya tiene una cita reservada el día " + fecha + " en el bloque " + hora + ". Seleccione otro bloque o doctor.");
        }
    }

    // Id único: los últimos números de la hora actual + un contador (por si se crean dos en el mismo instante)
    private String nuevoId() {
        String id;
        do {
            id = "CIT-" + String.valueOf(System.currentTimeMillis()).substring(7) + "-" + contador.incrementAndGet();
        } while (repo.existsById(id));
        return id;
    }
}
