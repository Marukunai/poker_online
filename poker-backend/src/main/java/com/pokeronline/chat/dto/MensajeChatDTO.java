package com.pokeronline.chat.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeChatDTO {
    private Long mesaId;
    private Long remitenteId;
    private String remitenteUsername;
    private String contenido;
    private Date timestamp;
    private Long torneoId;
}