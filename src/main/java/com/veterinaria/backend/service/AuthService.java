package com.veterinaria.backend.service;

import com.veterinaria.backend.exception.ApiException;
import com.veterinaria.backend.model.Usuario;
import com.veterinaria.backend.repository.UsuarioRepository;
import com.veterinaria.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

// Inicio de sesión y renovación de tokens.
@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    // Valida correo y clave. Si todo está bien devuelve los tokens y los datos básicos del usuario.
    public Map<String, Object> login(String correo, String clave) {
        if (correo == null || correo.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "El campo \"Correo Electrónico\" es obligatorio.");
        if (clave == null || clave.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "El campo \"Contraseña\" es obligatorio.");

        String mail = correo.trim().toLowerCase();
        Usuario u = usuarios.findByCorreoIgnoreCase(mail).orElseThrow(() ->
                new ApiException(HttpStatus.UNAUTHORIZED, "El correo \"" + mail + "\" no se encuentra registrado en el sistema."));
        // La clave se compara contra el cifrado guardado en la base de datos
        if (!encoder.matches(clave.trim(), u.getClave())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La contraseña ingresada es incorrecta.");
        }
        // Si el administrador desactivó la cuenta, no entra aunque la clave esté bien
        if (!u.isActivo()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esta cuenta está desactivada. Contacta al administrador de la clínica.");
        }
        return respuesta(u);
    }

    // Con un token de renovación válido entrega un token de acceso nuevo (y uno de renovación nuevo)
    public Map<String, Object> renovar(String tokenRenovacion) {
        try {
            Claims datos = jwt.leer(tokenRenovacion);
            if (!"renovacion".equals(datos.get("tipo", String.class))) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "El token no sirve para renovar la sesión.");
            }
            Usuario u = usuarios.findById(Long.valueOf(datos.getSubject()))
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "La sesión ya no es válida."));
            if (!u.isActivo()) throw new ApiException(HttpStatus.FORBIDDEN, "Esta cuenta está desactivada.");
            return respuesta(u);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La sesión venció. Inicia sesión nuevamente.");
        }
    }

    // Solo se devuelven los datos necesarios (nombre, correo y rol). La contraseña nunca sale del backend.
    private Map<String, Object> respuesta(Usuario u) {
        Map<String, Object> usuario = new LinkedHashMap<>();
        usuario.put("nombre", u.getNombre());
        usuario.put("correo", u.getCorreo());
        usuario.put("rol", u.getRol());

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("token", jwt.generarTokenAcceso(u));
        r.put("tokenRenovacion", jwt.generarTokenRenovacion(u));
        r.put("usuario", usuario);
        return r;
    }
}
