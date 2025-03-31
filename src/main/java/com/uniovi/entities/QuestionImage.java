package com.uniovi.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.entities.abstracts.AbstractQuestion;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class QuestionImage extends AbstractQuestion<AnswerImage> {

    private String imageUrl;

    public QuestionImage(String statement, List<AnswerImage> options, AnswerImage correctAnswer, Category category, String language, String imageUrl) {
        super(statement, options, correctAnswer, category, language);
        setImageUrl(imageUrl);
    }

    public QuestionImage(String statement, List<AnswerImage> options, AnswerImage correctAnswer, Category category, Language language, String imageUrl) {
        super(statement, options, correctAnswer, category, language);
        setImageUrl(imageUrl);
    }

    @Override
    protected void doOptionsAssociation(List<AnswerImage> options) {
        Associations.QuestionImageAnswers.addAnswer(this, options);
    }

    @Override
    public String toString() {
        return "QuestionImage{" +
                "statement='" + getStatement() + '\'' +
                ", options=" + getOptions().toString() +
                ", correctAnswer=" + getCorrectAnswer().toString() +
                ", category=" + getCorrectAnswer() +
                ", imageUrl=" + getImageUrl() +
                '}';
    }

    @Override
    public JsonNode toJson() {
        ObjectNode obj = (ObjectNode) super.toJson();
        obj.put("imageUrl", imageUrl);
        return obj;
    }
}
