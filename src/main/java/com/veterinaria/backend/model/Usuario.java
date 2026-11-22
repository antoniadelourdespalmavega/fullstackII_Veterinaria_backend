package com.veterinaria.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Tabla USUARIOS: quién puede entrar al sistema y con qué rol.
@Entity
@Table(name = "usuarios")
public class Usuario {

    public static final String ADMINISTRADOR = "administrador";
    public static final String SECRETARIA = "secretaria";
    public static final String VETERINARIO = "veterinario";
    public static final String CLIENTE = "cliente";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres.")
    private String nombre;

    // El correo es único: no puede haber dos usuarios con el mismo
    @NotBlank(message = "El correo es obligatorio.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(cl|com)$", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "El correo debe ser válido (@ y terminar en .cl o .com).")
    @Column(unique = true, nullable = false)
    private String correo;

    // La contraseña se guarda cifrada (BCrypt) y NUNCA se devuelve en las respuestas de la API
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String clave;

    // administrador | secretaria | veterinario | cliente (el servicio valida que sea uno de estos)
    private String rol;

    private String telefono;

    // "Base", "Registro Web", "Creado por Admin", "Personal Clínico" o "Modificado"
    private String tipo;

    // Una cuenta desactivada no puede iniciar sesión (no se borra)
    private boolean activo = true;

    public Usuario() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
