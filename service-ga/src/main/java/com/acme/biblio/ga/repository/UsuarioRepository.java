package com.acme.biblio.ga.repository;

import com.acme.biblio.ga.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}
