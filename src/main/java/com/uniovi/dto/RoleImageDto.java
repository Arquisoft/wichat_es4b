package com.uniovi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleImageDto {

    @Schema(description = "The name of the role", example = "ROLE_USER")
    private String name;
}
