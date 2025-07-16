package com.pokeronline.admin.dto;

import com.pokeronline.model.Role;
import lombok.Data;

@Data
public class UpdateUserDTO {
    private String username;
    private String email;
    private Role role;
    private int fichas;
}
