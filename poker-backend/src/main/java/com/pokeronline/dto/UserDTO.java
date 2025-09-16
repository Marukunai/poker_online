package com.pokeronline.dto;

import com.pokeronline.bot.DificultadBot;
import com.pokeronline.bot.EstiloBot;
import com.pokeronline.moderacion.dto.SancionDTO;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private int fichas;
    private int partidasGanadas;
    private int manosJugadas;
    private int manosGanadas;
    private int vecesAllIn;
    private int fichasGanadasHistoricas;
    private int vecesHizoBluff;
    private DificultadBot nivelBot;
    private EstiloBot estiloBot;
    private List<SancionDTO> sanciones;
    private boolean bloqueado;
    private boolean chatBloqueado;
}