package com.acme.biblio.ga.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LIBRO")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Libro {

    @Id
    @Column(name = "LIBRO_ID")
    private String id;

    private String titulo;

    @Column(name = "STOCK_SEDE_A")
    private Integer stockSedeA;

    @Column(name = "STOCK_SEDE_B")
    private Integer stockSedeB;
}
