package com.uniovi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@AllArgsConstructor
public class RoleDto {

    @Schema(description = "The name of the role", example = "ROLE_USER")
    private String name;
}
