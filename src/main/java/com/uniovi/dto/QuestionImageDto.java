package com.uniovi.dto;

import com.uniovi.entities.Question;
import com.uniovi.entities.QuestionImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class QuestionImageDto extends QuestionBaseDto {


    @Schema(description = "The imageUrl of the question")
    private String imageUrl;

    public QuestionImageDto(QuestionImage questionImage) {
        super(questionImage);
        imageUrl = questionImage.getImageUrl();
    }

}
