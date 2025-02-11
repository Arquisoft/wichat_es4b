package com.uniovi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AnswerDto {

    @Schema(description = "The text of the answer", example = "The answer text")
    private String text;

    @Schema(description = "Whether the answer is correct or not", example = "true")
    private boolean correct;

}
