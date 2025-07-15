package com.pokeronline.torneo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlindLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int nivel;
    private int smallBlind;
    private int bigBlind;
    private int duracionSegundos;

    @ManyToOne
    private Torneo torneo;
}
