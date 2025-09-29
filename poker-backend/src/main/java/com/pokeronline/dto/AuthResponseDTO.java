package com.pokeronline.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
    private String role;
}