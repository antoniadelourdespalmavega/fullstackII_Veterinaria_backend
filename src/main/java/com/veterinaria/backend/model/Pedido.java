package com.veterinaria.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// Tabla PEDIDOS: cada compra que se hace en la tienda (después de que el pago se aprueba).
// Los productos comprados quedan en la tabla PEDIDO_ITEMS. Los datos de la tarjeta NO se guardan.
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del comprador es obligatorio.")
    private String nombre;

    @NotBlank(message = "El correo del comprador es obligatorio.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(cl|com)$", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "El correo debe ser válido (@ y terminar en .cl o .com).")
    private String correo;

    // "Tarjeta" o "Transferencia"
    @NotBlank(message = "El método de pago es obligatorio.")
    private String metodo;

    // Código que entregó la pasarela de pago, por ejemplo TXN-245910
    @NotBlank(message = "El código de transacción es obligatorio.")
    private String codigoTransaccion;

    // Lo calcula el backend sumando los productos (no se confía en el valor que mande el frontend)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long total;

    // Cuándo se compró. Lo pone el backend.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(updatable = false)
    private Instant creadoEn;

    @Valid
    @NotEmpty(message = "El pedido debe tener al menos un producto.")
    // EAGER: al leer un pedido se leen también sus productos (son pocos), así se pueden convertir a JSON
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id")
    private List<PedidoItem> items = new ArrayList<>();

    public Pedido() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getCodigoTransaccion() { return codigoTransaccion; }
    public void setCodigoTransaccion(String codigoTransaccion) { this.codigoTransaccion = codigoTransaccion; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public List<PedidoItem> getItems() { return items; }
    public void setItems(List<PedidoItem> items) { this.items = items; }
}
