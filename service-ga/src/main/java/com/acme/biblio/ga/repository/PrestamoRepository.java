package com.acme.biblio.ga.repository;

import com.acme.biblio.ga.domain.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    // Buscar todos los préstamos de un usuario (asumiendo campo: Prestamo.usuario.usuarioId)
    List<Prestamo> findByUsuarioUsuarioId(String usuarioId);

    // Buscar todos los préstamos de un libro (asumiendo campo: Prestamo.libro.libroId)
    List<Prestamo> findByLibroLibroId(String libroId);

    // Buscar por estado (ACTIVO, DEV, etc.)
    List<Prestamo> findByEstado(String estado);

    // 👉 Clave para renovación y devolución:
    // Último préstamo ACTIVO de ese usuario y libro
    Optional<Prestamo> findTopByUsuarioUsuarioIdAndLibroLibroIdAndEstadoOrderByFechaInicioDesc(
            String usuarioId,
            String libroId,
            String estado
    );
}
