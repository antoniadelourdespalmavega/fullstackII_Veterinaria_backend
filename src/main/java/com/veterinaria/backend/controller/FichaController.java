package com.veterinaria.backend.controller;

import com.veterinaria.backend.model.Ficha;
import com.veterinaria.backend.service.FichaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// CRUD de fichas clínicas. El cliente solo puede leer las de sus mascotas.
@RestController
@RequestMapping("/api/fichas")
@Tag(name = "Fichas clínicas", description = "Historial clínico de los pacientes")
public class FichaController {

    private final FichaService service;

    public FichaController(FichaService service) {
        this.service = service;
    }

    private static String rol(Authentication a) {
        return a.getAuthorities().iterator().next().getAuthority().substring(5).toLowerCase();
    }

    @GetMapping
    @Operation(summary = "Listar fichas", description = "El personal ve todas; el cliente solo las de sus mascotas.")
    public List<Ficha> listar(Authentication a) {
        return service.listar(a.getName(), rol(a));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una ficha por id")
    public Ficha obtener(@PathVariable String id, Authentication a) {
        return service.obtener(id, a.getName(), rol(a));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('VETERINARIO','SECRETARIA','ADMINISTRADOR')")
    @Operation(summary = "Crear una ficha de paciente", description = "El id (PAC-xxx) lo genera el backend. Queda sin pagar.")
    public Ficha crear(@RequestBody Ficha datos) {
        return service.crear(datos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('VETERINARIO','SECRETARIA','ADMINISTRADOR')")
    @Operation(summary = "Actualizar una ficha", description = "Sirve para editar el diagnóstico, el tratamiento, el estado y para registrar el pago.")
    public Ficha actualizar(@PathVariable String id, @RequestBody Ficha datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar una ficha definitivamente (solo administrador)")
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
