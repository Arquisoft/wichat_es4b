package com.uniovi.dto;

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
public class QuestionImageDto {

    @Schema(description = "The statement of the questionImage")
    private String statement;

    @Schema(description = "The options of the questionImage")
    private List<AnswerImageDto> options;

    @Schema(description = "The correct answer of the questionImage", hidden = true)
    private AnswerImageDto correctAnswer;

    @Schema(description = "The category of the questionImage")
    private CategoryImageDto category;

    @Schema(description = "The language of the questionImage")
    private String language;

    @Schema(description = "The imageUrl of the questionImage")
    private String imageUrl;

    public QuestionImageDto(QuestionImage questionImage) {
        statement = questionImage.getStatement();
        options = questionImage.getOptions().stream().map(a -> new AnswerImageDto(a.getText(), a.isCorrect())).toList();
        correctAnswer = new AnswerImageDto(questionImage.getCorrectAnswer().getText(), questionImage.getCorrectAnswer().isCorrect());
        category= new CategoryImageDto(questionImage.getCategory().getName(), questionImage.getCategory().getDescription(), new ArrayList<>());
        language = questionImage.getLanguage();
        imageUrl = questionImage.getImageUrl();
    }

}
