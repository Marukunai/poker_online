package com.pokeronline.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdatePasswordDTO {
    private String currentPassword;
    private String newPassword;
}