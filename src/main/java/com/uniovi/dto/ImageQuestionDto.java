package com.uniovi.dto;

import com.uniovi.entities.ImageQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ImageQuestionDto {

    @Schema(description = "The statement of the image question")
    private String statement;

    @Schema(description = "The options of the image question")
    private List<AnswerDto> options;

    @Schema(description = "The correct answer of the image question", hidden = true)
    private AnswerDto correctAnswer;

    @Schema(description = "The category of the image question")
    private CategoryDto category;

    @Schema(description = "The language of the image question")
    private String language;

    @Schema(description = "The URL of the image associated with the question")
    private String imageUrl;

    public ImageQuestionDto(ImageQuestion imageQuestion) {
        statement = imageQuestion.getStatement();
        options = imageQuestion.getOptions().stream().map(a -> new AnswerDto(a.getText(), a.isCorrect())).toList();
        correctAnswer = new AnswerDto(imageQuestion.getCorrectAnswer().getText(), imageQuestion.getCorrectAnswer().isCorrect());
        category = new CategoryDto(imageQuestion.getCategory().getName(), imageQuestion.getCategory().getDescription(), new ArrayList<>());
        language = imageQuestion.getLanguage();
        imageUrl = imageQuestion.getImageUrl();
    }
}
