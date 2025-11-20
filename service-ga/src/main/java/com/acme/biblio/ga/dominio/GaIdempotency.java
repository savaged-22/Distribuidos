package com.acme.biblio.ga.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "GA_IDEMPOTENCY")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GaIdempotency {

    @Id
    @Column(name = "IDEMPOTENCY_KEY")
    private String id;

    @Column(name = "CREATED_AT")
    private LocalDate createdAt;

    @Column(name = "OP_SUMMARY")
    private String summary;
}
