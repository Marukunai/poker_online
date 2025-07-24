package com.pokeronline.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Entity
@Data
public class RegistroAbandono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Mesa mesa;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

}