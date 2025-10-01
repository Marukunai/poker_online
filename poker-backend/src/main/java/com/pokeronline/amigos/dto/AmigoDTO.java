package com.pokeronline.amigos.dto;

import com.pokeronline.amigos.model.EstadoConexion;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmigoDTO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private EstadoConexion estado;
    private String detalleEstado;
    private LocalDateTime ultimaConexion;
    private Boolean esFavorito;
    private String alias;
    private Boolean puedeUnirse;
    private Long mesaId;
    private Long torneoId;
    private Long fichas;
}