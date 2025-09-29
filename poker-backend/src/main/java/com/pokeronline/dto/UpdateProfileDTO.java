package com.pokeronline.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileDTO {
    private String username;
    private String avatarUrl;
}