package com.veterinaria.backend.controller;

import com.veterinaria.backend.model.Usuario;
import com.veterinaria.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// CRUD de usuarios. Solo lo puede usar el administrador.
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Usuarios", description = "Gestión de usuarios (solo administrador)")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    public List<Usuario> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por id")
    public Usuario obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un usuario con cualquier rol")
    public Usuario crear(@Valid @RequestBody Usuario datos) {
        return service.crear(datos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar nombre, correo y rol de un usuario")
    public Usuario editar(@PathVariable Long id, @Valid @RequestBody Usuario datos) {
        return service.editar(id, datos);
    }

    @PatchMapping("/{id}/activo")
    @Operation(summary = "Activar o desactivar una cuenta", description = "Una cuenta desactivada no puede iniciar sesión.")
    public Usuario alternarActivo(@PathVariable Long id, Authentication quienPide) {
        return service.alternarActivo(id, quienPide.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un usuario definitivamente")
    public void eliminar(@PathVariable Long id, Authentication quienPide) {
        service.eliminar(id, quienPide.getName());
    }
}
