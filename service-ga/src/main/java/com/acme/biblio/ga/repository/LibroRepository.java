package com.acme.biblio.ga.repository;

import com.acme.biblio.ga.domain.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, String> {
}
