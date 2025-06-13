package com.pokeronline.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private int fichas;
}
