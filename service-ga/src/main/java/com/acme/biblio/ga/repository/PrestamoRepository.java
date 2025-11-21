package com.acme.biblio.ga.repository;

import com.acme.biblio.ga.domain.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    // Listar por usuario (usa la asociación Prestamo.usuario.id)
    List<Prestamo> findByUsuario_Id(String usuarioId);

    // Listar por libro (usa Prestamo.libro.id)
    List<Prestamo> findByLibro_Id(String libroId);

    // Listar por estado
    List<Prestamo> findByEstado(String estado);

    // Último préstamo ACTIVO (u otro estado) de un usuario para un libro
    Optional<Prestamo> findTopByUsuario_IdAndLibro_IdAndEstadoOrderByFechaEntregaDesc(
            String usuarioId,
            String libroId,
            String estado
    );
}
