package com.veterinaria.backend.controller;

import com.veterinaria.backend.model.Pedido;
import com.veterinaria.backend.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Compras de la tienda. Comprar es público (la tienda se usa sin iniciar sesión);
// ver las ventas es solo para el administrador y la secretaria.
@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Compras realizadas en la tienda")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una compra (público)", description = "Se llama cuando el pago fue aprobado. El total lo calcula el backend. No se guardan datos de tarjeta.")
    public Pedido crear(@Valid @RequestBody Pedido datos) {
        return service.crear(datos);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SECRETARIA')")
    @Operation(summary = "Listar todas las compras (los más nuevos primero)")
    public List<Pedido> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SECRETARIA')")
    @Operation(summary = "Obtener una compra por id")
    public Pedido obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar una compra definitivamente (solo administrador)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
