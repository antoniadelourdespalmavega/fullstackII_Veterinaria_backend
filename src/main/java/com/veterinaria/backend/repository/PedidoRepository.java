package com.veterinaria.backend.repository;

import com.veterinaria.backend.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Acceso a la tabla PEDIDOS (los más nuevos primero).
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findAllByOrderByCreadoEnDesc();
}
