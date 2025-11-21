package com.acme.biblio.ga.repository;

import com.acme.biblio.ga.domain.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    // Consultas simples por asociaciones (opcionales pero útiles)
    List<Prestamo> findByUsuarioUsuarioId(String usuarioId);

    List<Prestamo> findByLibroLibroId(String libroId);

    List<Prestamo> findByEstado(String estado);

    /**
     * Devuelve el último préstamo (por fechaInicio DESC) para un usuario, libro y estado.
     * Se usa en DevolucionService y RenovacionService.
     *
     * Traducción del nombre:
     *   usuario.usuarioId  -> UsuarioUsuarioId
     *   libro.libroId      -> LibroLibroId
     */
    Optional<Prestamo> findTopByUsuarioUsuarioIdAndLibroLibroIdAndEstadoOrderByFechaInicioDesc(
            String usuarioId,
            String libroId,
            String estado
    );
}
