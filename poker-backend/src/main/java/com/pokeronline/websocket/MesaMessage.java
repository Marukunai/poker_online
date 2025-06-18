package com.pokeronline.websocket;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MesaMessage {
    private String tipo;  // "accion", "turno", "fase", etc.
    private Long mesaId;
    private Object payload;
}