package com.pokeronline.logros.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String descripcion;

    private String iconoLogro; // "files/images/mi_logro.png"ç

    @Enumerated(EnumType.STRING)
    private CategoriaLogro categoria;
}