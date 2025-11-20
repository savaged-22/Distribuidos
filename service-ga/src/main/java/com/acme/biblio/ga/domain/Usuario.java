package com.acme.biblio.ga.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USUARIO")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Usuario {

    @Id
    @Column(name = "USUARIO_ID")
    private String id;

    private String nombre;
}
