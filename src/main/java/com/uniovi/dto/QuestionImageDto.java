package com.uniovi.dto;

import com.uniovi.entities.QuestionImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class QuestionImageDto extends QuestionDto {

    @Schema(description = "The imageUrl of the questionImage")
    private String imageUrl;

    public QuestionImageDto(QuestionImage questionImage) {
        super(questionImage);
        setImageUrl(questionImage.getImageUrl());
    }

}
