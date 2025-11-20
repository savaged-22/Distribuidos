package com.acme.biblio.ga.repository;

import com.acme.biblio.ga.domain.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuarioId(String usuarioId);

    List<Prestamo> findByLibroId(String libroId);

    List<Prestamo> findByEstado(String estado);
}
