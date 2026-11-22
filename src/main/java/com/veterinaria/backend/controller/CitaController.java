package com.veterinaria.backend.controller;

import com.veterinaria.backend.model.Cita;
import com.veterinaria.backend.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// CRUD de citas. Los permisos dependen del rol (se ven en cada @PreAuthorize).
@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Solicitud, agenda, reagendado y anulación de citas")
public class CitaController {

    private final CitaService service;

    public CitaController(CitaService service) {
        this.service = service;
    }

    // Datos que se necesitan para confirmar o reagendar una cita
    public record GestionCita(String doctor, String fecha, String hora, String modo) {
    }

    private static String rol(Authentication a) {
        return a.getAuthorities().iterator().next().getAuthority().substring(5).toLowerCase();
    }

    @GetMapping
    @Operation(summary = "Listar citas", description = "El personal ve todas; el cliente solo las suyas.")
    public List<Cita> listar(Authentication a) {
        return service.listar(a.getName(), rol(a));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una cita por id")
    public Cita obtener(@PathVariable String id, Authentication a) {
        return service.obtener(id, a.getName(), rol(a));
    }

    @GetMapping("/horarios-ocupados")
    @Operation(summary = "Horas ya reservadas en un día (público)", description = "Devuelve solo las horas, sin datos personales. Se usa en el formulario de solicitud de cita.")
    public List<String> horariosOcupados(@RequestParam String fecha) {
        return service.horasOcupadas(fecha);
    }

    @PostMapping("/solicitud")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Solicitar una cita (público)", description = "Queda Pendiente y sin veterinario hasta que Recepción la confirme.")
    public Cita solicitar(@RequestBody Cita datos) {
        return service.solicitar(datos);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SECRETARIA','ADMINISTRADOR')")
    @Operation(summary = "Agendar una cita presencial", description = "Queda Confirmada. Falla si el veterinario ya tiene esa hora.")
    public Cita agendarPresencial(@RequestBody Cita datos) {
        return service.agendarPresencial(datos);
    }

    @PutMapping("/{id}/gestion")
    @PreAuthorize("hasAnyRole('SECRETARIA','ADMINISTRADOR')")
    @Operation(summary = "Confirmar una solicitud o reagendar una cita", description = "modo = confirmar | reagendar")
    public Cita gestionar(@PathVariable String id, @RequestBody GestionCita g) {
        return service.gestionar(id, g.doctor(), g.fecha(), g.hora(), g.modo());
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('SECRETARIA','ADMINISTRADOR')")
    @Operation(summary = "Anular o rechazar una cita", description = "No se borra: queda en estado Cancelada.")
    public Cita anular(@PathVariable String id) {
        return service.anular(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar una cita definitivamente (solo administrador)")
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
