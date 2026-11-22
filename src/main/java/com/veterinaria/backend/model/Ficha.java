package com.veterinaria.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// Tabla FICHAS: la ficha clínica de cada paciente (mascota), con su diagnóstico, tratamiento y pago.
@Entity
@Table(name = "fichas")
public class Ficha {

    public static final String EN_TRATAMIENTO = "En Tratamiento";
    public static final String FINALIZADO = "Finalizado";

    // Ejemplo: PAC-001 (el servicio genera los nuevos)
    @Id
    private String id;

    private String mascota;
    private String especie;
    private String raza;
    private String edad;
    private String peso;
    private String dueno;
    private String telefono;
    private String correo;
    @Column(length = 500)
    private String condiciones;
    @Column(length = 500)
    private String problema;
    @Column(length = 1000)
    private String tratamiento;
    // En Tratamiento | Finalizado
    private String estado;
    // true cuando Recepción ya cobró
    private boolean pagado;
    // Datos del cobro (queda vacío mientras no se paga)
    @Embedded
    private DetallePago detallePago;
    // Cuándo se creó: sirve para mostrar primero lo más nuevo. Lo pone el backend.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(updatable = false)
    private Instant creadoEn;

    public Ficha() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMascota() { return mascota; }
    public void setMascota(String mascota) { this.mascota = mascota; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getEdad() { return edad; }
    public void setEdad(String edad) { this.edad = edad; }
    public String getPeso() { return peso; }
    public void setPeso(String peso) { this.peso = peso; }
    public String getDueno() { return dueno; }
    public void setDueno(String dueno) { this.dueno = dueno; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getCondiciones() { return condiciones; }
    public void setCondiciones(String condiciones) { this.condiciones = condiciones; }
    public String getProblema() { return problema; }
    public void setProblema(String problema) { this.problema = problema; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isPagado() { return pagado; }
    public void setPagado(boolean pagado) { this.pagado = pagado; }
    public DetallePago getDetallePago() { return detallePago; }
    public void setDetallePago(DetallePago detallePago) { this.detallePago = detallePago; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
