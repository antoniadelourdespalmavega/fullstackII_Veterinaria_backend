package com.veterinaria.backend.service;

import com.veterinaria.backend.exception.ApiException;
import com.veterinaria.backend.model.Usuario;
import com.veterinaria.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

// Lógica de negocio de los usuarios: validaciones, correo único, contraseña cifrada y desactivar cuentas.
@Service
@Transactional
public class UsuarioService {

    private static final Set<String> ROLES = Set.of(
            Usuario.ADMINISTRADOR, Usuario.SECRETARIA, Usuario.VETERINARIO, Usuario.CLIENTE);

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se encontró el usuario."));
    }

    // El administrador crea un usuario con cualquier rol
    public Usuario crear(Usuario datos) {
        validarRol(datos.getRol());
        validarClave(datos.getClave());
        correoLibre(datos.getCorreo(), null);

        Usuario u = new Usuario();
        u.setNombre(datos.getNombre().trim());
        u.setCorreo(datos.getCorreo().trim().toLowerCase());
        u.setClave(encoder.encode(datos.getClave().trim()));
        u.setRol(datos.getRol());
        u.setTelefono(datos.getTelefono());
        u.setTipo(datos.getTipo() != null && !datos.getTipo().isBlank() ? datos.getTipo()
                : (Usuario.CLIENTE.equals(datos.getRol()) ? "Creado por Admin" : "Personal Clínico"));
        u.setActivo(true);
        return repo.save(u);
    }

    // Registro desde la página "Crear Cuenta": siempre queda con rol cliente
    public Usuario registrarCliente(Usuario datos) {
        validarClave(datos.getClave());
        if (repo.existsByCorreoIgnoreCase(datos.getCorreo())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta asociada a este correo. Intenta iniciar sesión.");
        }
        Usuario u = new Usuario();
        u.setNombre(datos.getNombre().trim());
        u.setCorreo(datos.getCorreo().trim().toLowerCase());
        u.setClave(encoder.encode(datos.getClave().trim()));
        u.setTelefono(datos.getTelefono() == null ? null : datos.getTelefono().trim());
        u.setRol(Usuario.CLIENTE);
        u.setTipo("Registro Web");
        u.setActivo(true);
        return repo.save(u);
    }

    // Edita nombre, correo y rol. La contraseña y el estado (activo) se mantienen.
    public Usuario editar(Long id, Usuario datos) {
        Usuario u = obtener(id);
        validarRol(datos.getRol());
        correoLibre(datos.getCorreo(), id);

        u.setNombre(datos.getNombre().trim());
        u.setCorreo(datos.getCorreo().trim().toLowerCase());
        u.setRol(datos.getRol());
        if ("Base".equals(u.getTipo())) u.setTipo("Modificado");
        return repo.save(u);
    }

    // Desactiva o reactiva una cuenta (no se borra). Una cuenta desactivada no puede iniciar sesión.
    public Usuario alternarActivo(Long id, String correoQuienPide) {
        Usuario u = obtener(id);
        if (u.getCorreo().equalsIgnoreCase(correoQuienPide) && u.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propia cuenta.");
        }
        u.setActivo(!u.isActivo());
        return repo.save(u);
    }

    public void eliminar(Long id, String correoQuienPide) {
        Usuario u = obtener(id);
        if (u.getCorreo().equalsIgnoreCase(correoQuienPide)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta.");
        }
        repo.delete(u);
    }

    private void validarRol(String rol) {
        if (rol == null || !ROLES.contains(rol)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El rol no es válido.");
        }
    }

    private void validarClave(String clave) {
        if (clave == null || clave.trim().length() < 4) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 4 caracteres.");
        }
    }

    // El correo no puede estar usado por otro usuario (idPropio = el usuario que se está editando)
    private void correoLibre(String correo, Long idPropio) {
        repo.findByCorreoIgnoreCase(correo.trim()).ifPresent(otro -> {
            if (!otro.getId().equals(idPropio)) {
                throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.");
            }
        });
    }
}
