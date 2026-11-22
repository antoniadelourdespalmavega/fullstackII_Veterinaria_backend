package com.veterinaria.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// Tabla CITAS: la agenda de la clínica. El estado puede ser Pendiente, Confirmada o Cancelada.
@Entity
@Table(name = "citas")
public class Cita {

    public static final String PENDIENTE = "Pendiente";
    public static final String CONFIRMADA = "Confirmada";
    public static final String CANCELADA = "Cancelada";

    // Ejemplo: CIT-101 (el servicio genera los nuevos)
    @Id
    private String idCita;

    // Fecha en formato AAAA-MM-DD
    private String fecha;
    // Hora o bloque, por ejemplo "10:00 - 10:30" o "12:30"
    private String hora;
    private String dueno;
    private String telefono;
    private String correo;
    private String mascota;
    private String especie;
    private String raza;
    private String doctor;
    @Column(length = 500)
    private String motivo;
    private String estado;
    // Si la cita se reagenda, aquí queda dónde estaba antes
    private String fechaAnterior;
    // Cuándo se creó: sirve para mostrar primero lo más nuevo. Lo pone el backend.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(updatable = false)
    private Instant creadoEn;

    public Cita() {
    }

    public String getIdCita() { return idCita; }
    public void setIdCita(String idCita) { this.idCita = idCita; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getDueno() { return dueno; }
    public void setDueno(String dueno) { this.dueno = dueno; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getMascota() { return mascota; }
    public void setMascota(String mascota) { this.mascota = mascota; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFechaAnterior() { return fechaAnterior; }
    public void setFechaAnterior(String fechaAnterior) { this.fechaAnterior = fechaAnterior; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
