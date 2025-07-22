package com.pokeronline.torneo.model;

import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsperaTorneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Torneo torneo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestampIngreso;
}