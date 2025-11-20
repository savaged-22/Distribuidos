package com.acme.biblio.ga.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "PRESTAMO")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRESTAMO_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "LIBRO_ID")
    private Libro libro;

    @ManyToOne
    @JoinColumn(name = "USUARIO_ID")
    private Usuario usuario;

    @Column(name = "FECHA_INICIO")
    private LocalDate fechaInicio;

    @Column(name = "FECHA_ENTREGA")
    private LocalDate fechaEntrega;

    private Integer renovaciones;

    private String estado;
}
