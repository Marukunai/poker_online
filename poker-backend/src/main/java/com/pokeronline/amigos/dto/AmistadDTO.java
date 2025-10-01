package com.pokeronline.amigos.dto;

import com.pokeronline.amigos.model.Amistad;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmistadDTO {
    private Long id;
    private Long amigoId;
    private String amigoUsername;
    private LocalDateTime fechaAmistad;
    private Boolean esFavorito;
    private String alias;

    public static AmistadDTO fromEntity(Amistad a, Long viewerId) {
        boolean viewerEsU1 = a.getUsuario1().getId().equals(viewerId);
        var amigo = viewerEsU1 ? a.getUsuario2() : a.getUsuario1();
        return AmistadDTO.builder()
                .id(a.getId())
                .amigoId(amigo.getId())
                .amigoUsername(amigo.getUsername())
                .fechaAmistad(a.getFechaAmistad())
                .esFavorito(viewerEsU1 ? a.getEsFavorito1() : a.getEsFavorito2())
                .alias(viewerEsU1 ? a.getAlias1() : a.getAlias2())
                .build();
    }
}