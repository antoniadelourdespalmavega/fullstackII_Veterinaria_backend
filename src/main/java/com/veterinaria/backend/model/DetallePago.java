package com.veterinaria.backend.model;

import jakarta.persistence.Embeddable;

// Datos del cobro de una ficha. No es una tabla aparte: sus columnas quedan dentro de FICHAS.
@Embeddable
public class DetallePago {

    private String monto;
    private String metodo;
    private String concepto;
    private String fechaPago;
    private String atendidoPor;

    public DetallePago() {
    }

    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public String getFechaPago() { return fechaPago; }
    public void setFechaPago(String fechaPago) { this.fechaPago = fechaPago; }
    public String getAtendidoPor() { return atendidoPor; }
    public void setAtendidoPor(String atendidoPor) { this.atendidoPor = atendidoPor; }
}
