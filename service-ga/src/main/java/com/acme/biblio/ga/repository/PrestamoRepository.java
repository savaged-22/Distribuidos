package com.acme.biblio.ga.repository;

import com.acme.biblio.ga.domain.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    // Opcional: si lo usas en otros lados
    List<Prestamo> findByEstado(String estado);

    // ⭐ Método usado por DevolucionService y RenovacionService
    Optional<Prestamo> findTopByUsuario_IdAndLibro_IdAndEstadoOrderByFechaInicioDesc(
            String usuarioId,
            String libroId,
            String estado
    );
}