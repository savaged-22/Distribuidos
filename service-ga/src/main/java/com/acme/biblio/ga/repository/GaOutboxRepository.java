package com.acme.biblio.ga.repository;

import com.acme.biblio.ga.domain.GaOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GaOutboxRepository extends JpaRepository<GaOutbox, Long> {

    List<GaOutbox> findByProcessedAtIsNull();
}
