package com.veterinaria.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// Tabla PEDIDO_ITEMS: cada producto o servicio que se compró dentro de un pedido.
@Entity
@Table(name = "pedido_items")
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio.")
    private String nombre;

    // Precio en pesos chilenos
    @Min(value = 1, message = "El precio de cada producto debe ser mayor a 0.")
    private long precio;

    public PedidoItem() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public long getPrecio() { return precio; }
    public void setPrecio(long precio) { this.precio = precio; }
}
