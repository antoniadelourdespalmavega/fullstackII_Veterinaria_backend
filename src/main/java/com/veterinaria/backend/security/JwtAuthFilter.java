package com.veterinaria.backend.security;

import com.veterinaria.backend.model.Usuario;
import com.veterinaria.backend.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Se ejecuta en CADA petición: si trae "Authorization: Bearer <token>" válido, deja al usuario autenticado
// con su rol. Si no trae token o es inválido, la petición sigue sin autenticar (y Spring Security la rechaza
// cuando el recurso es privado).
// Si app.swagger.sin-token=true, las peticiones hechas desde Swagger sin token entran como administrador.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;
    private final boolean swaggerSinToken;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarios,
                         @Value("${app.swagger.sin-token:false}") boolean swaggerSinToken) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
        this.swaggerSinToken = swaggerSinToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            try {
                Claims datos = jwtService.leer(cabecera.substring(7));
                // Solo sirve el token de ACCESO (el de renovación no abre recursos)
                if ("acceso".equals(datos.get("tipo", String.class))) {
                    // Confirmo en la base de datos que el usuario existe y sigue activo, y uso su rol actual
                    Usuario u = usuarios.findById(Long.valueOf(datos.getSubject())).orElse(null);
                    if (u != null && u.isActivo()) {
                        autenticar(u);
                    }
                }
            } catch (JwtException | IllegalArgumentException e) {
                // Token vencido, alterado o mal formado: se ignora y la petición queda sin autenticar
            }
        }

        // Modo Swagger sin token: si la petición viene desde la página de Swagger y no trae token,
        // entra como el administrador. Las peticiones de la app React NO pasan por aquí.
        if (swaggerSinToken
                && SecurityContextHolder.getContext().getAuthentication() == null
                && vieneDeSwagger(request)) {
            usuarios.findByCorreoIgnoreCase("admin@sanmarcos.cl")
                    .filter(Usuario::isActivo)
                    .ifPresent(this::autenticar);
        }

        chain.doFilter(request, response);
    }

    // Deja al usuario autenticado con su rol (ROLE_ADMINISTRADOR, ROLE_CLIENTE, etc.)
    private void autenticar(Usuario u) {
        var autenticacion = new UsernamePasswordAuthenticationToken(
                u.getCorreo(), null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().toUpperCase())));
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }

    // El navegador indica desde qué página se hizo la petición (cabecera Referer)
    private boolean vieneDeSwagger(HttpServletRequest request) {
        String origen = request.getHeader("Referer");
        return origen != null && origen.contains("/swagger-ui");
    }
}