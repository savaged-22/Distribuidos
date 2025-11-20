package com.acme.biblio.ga.repository;

import com.acme.biblio.ga.domain.GaIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GaIdempotencyRepository extends JpaRepository<GaIdempotency, String> {
}
