package com.acme.biblio.ga.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "GA_OUTBOX")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GaOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EVENT_TYPE")
    private String eventType;

    @Lob
    private String payload;

    @Column(name = "CREATED_AT")
    private LocalDate createdAt;

    @Column(name = "PROCESSED_AT")
    private LocalDate processedAt;
}
