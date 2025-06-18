package com.pokeronline.dto;

import lombok.*;

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
}
