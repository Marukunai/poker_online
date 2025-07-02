package com.pokeronline.torneo.model;

import com.pokeronline.model.Mesa;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TorneoMesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Torneo torneo;

    @OneToOne
    private Mesa mesa;

    private int ronda; // Para saber si es una mesa de fase inicial, semifinal, final, etc.
}