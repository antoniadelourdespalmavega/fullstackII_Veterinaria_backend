package com.veterinaria.backend.controller;

import com.veterinaria.backend.model.Usuario;
import com.veterinaria.backend.service.AuthService;
import com.veterinaria.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Endpoints PÚBLICOS: iniciar sesión, renovar el token y registrarse como cliente.
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login con JWT, renovación de token y registro de clientes")
public class AuthController {

    private final AuthService auth;
    private final UsuarioService usuarios;

    public AuthController(AuthService auth, UsuarioService usuarios) {
        this.auth = auth;
        this.usuarios = usuarios;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Recibe correo y clave. Devuelve el token de acceso, el token de renovación y los datos del usuario.")
    public Map<String, Object> login(@RequestBody Map<String, String> datos) {
        return auth.login(datos.get("correo"), datos.get("clave"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar el token", description = "Recibe el token de renovación y devuelve un token de acceso nuevo.")
    public Map<String, Object> renovar(@RequestBody Map<String, String> datos) {
        return auth.renovar(datos.get("tokenRenovacion"));
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un cliente", description = "Crea una cuenta nueva siempre con rol cliente.")
    public Usuario registro(@Valid @RequestBody Usuario datos) {
        return usuarios.registrarCliente(datos);
    }
}
