package com.veterinaria.backend.security;

import com.veterinaria.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Genera y verifica los tokens JWT.
// Hay dos tipos: el token de ACCESO (corto, 30 min) que se manda en cada petición,
// y el token de RENOVACIÓN (largo, 7 días) que sirve solo para pedir un token de acceso nuevo.
@Service
public class JwtService {

    private final SecretKey clave;
    private final long duracionAcceso;
    private final long duracionRenovacion;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.access-expiration-ms}") long duracionAcceso,
                      @Value("${app.jwt.refresh-expiration-ms}") long duracionRenovacion) {
        // La clave secreta firma los tokens: si alguien los modifica, la firma deja de coincidir
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionAcceso = duracionAcceso;
        this.duracionRenovacion = duracionRenovacion;
    }

    public String generarTokenAcceso(Usuario u) {
        return generar(u, "acceso", duracionAcceso);
    }

    public String generarTokenRenovacion(Usuario u) {
        return generar(u, "renovacion", duracionRenovacion);
    }

    private String generar(Usuario u, String tipo, long duracion) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(u.getId())) // a quién pertenece el token (su id: el correo puede cambiar)
                .claim("rol", u.getRol())        // su rol viaja dentro del token
                .claim("tipo", tipo)             // acceso o renovación
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracion))
                .signWith(clave)
                .compact();
    }

    // Verifica la firma y que no haya vencido. Devuelve los datos del token o lanza JwtException.
    public Claims leer(String token) throws JwtException {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }
}
