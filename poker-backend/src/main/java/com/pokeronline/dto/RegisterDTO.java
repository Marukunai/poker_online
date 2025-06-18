package com.pokeronline.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RegisterDTO {
    private String username;
    private String email;
    private String password;
}