package com.uniovi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    @Schema(description = "The name of the category", example = "Geography")
    private String name;

    @Schema(description = "The description of the category", example = "Questions about the world", hidden = true)
    private String description;

    @Schema(description = "The list of questions in the category", hidden = true)
    private List<QuestionDto> questions;
}
