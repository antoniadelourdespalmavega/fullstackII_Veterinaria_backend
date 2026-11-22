package com.veterinaria.backend.exception;

import org.springframework.http.HttpStatus;

// Error de negocio con el código HTTP que corresponde (400, 404, 409...).
public class ApiException extends RuntimeException {
    private final HttpStatus estado;

    public ApiException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
