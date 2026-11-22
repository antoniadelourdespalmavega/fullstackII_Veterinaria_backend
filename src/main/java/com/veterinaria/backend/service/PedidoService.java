package com.veterinaria.backend.service;

import com.veterinaria.backend.exception.ApiException;
import com.veterinaria.backend.model.Pedido;
import com.veterinaria.backend.model.PedidoItem;
import com.veterinaria.backend.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

// Lógica de negocio de los pedidos (las compras de la tienda).
@Service
@Transactional
public class PedidoService {

    private final PedidoRepository repo;

    public PedidoService(PedidoRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listar() {
        return repo.findAllByOrderByCreadoEnDesc();
    }

    @Transactional(readOnly = true)
    public Pedido obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se encontró el pedido."));
    }

    // Guarda la compra. El total lo calcula el backend sumando el precio de cada producto.
    public Pedido crear(Pedido datos) {
        Pedido p = new Pedido();
        p.setNombre(datos.getNombre().trim());
        p.setCorreo(datos.getCorreo().trim().toLowerCase());
        p.setMetodo(datos.getMetodo());
        p.setCodigoTransaccion(datos.getCodigoTransaccion());
        p.setCreadoEn(Instant.now());

        long total = 0;
        for (PedidoItem item : datos.getItems()) {
            PedidoItem nuevo = new PedidoItem();
            nuevo.setNombre(item.getNombre().trim());
            nuevo.setPrecio(item.getPrecio());
            p.getItems().add(nuevo);
            total += item.getPrecio();
        }
        p.setTotal(total);
        return repo.save(p);
    }

    public void eliminar(Long id) {
        repo.delete(obtener(id));
    }
}
