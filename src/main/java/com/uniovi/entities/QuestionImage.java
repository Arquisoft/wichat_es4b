package com.uniovi.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.interfaces.JsonEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class QuestionImage extends QuestionBase{

    private String imageUrl;

    public QuestionImage(String statement, List<Answer> optionsImage, Answer correctAnswerImage, Category categoryImage, String language, String imageUrl) {
        super(statement, optionsImage, correctAnswerImage, categoryImage, language);
        this.imageUrl = imageUrl;
    }

    public QuestionImage(String statement, List<Answer> optionsImage, Answer correctAnswerImage, Category categoryImage, Language language,String imageUrl) {
        super(statement, optionsImage, correctAnswerImage, categoryImage, language);
        this.imageUrl = imageUrl;
    }


    @Override
    public String toString() {
        return "QuestionImage{" +
                "statement='" + getStatement() + '\'' +
                ", options=" + getOptions().toString() +
                ", correctAnswer=" + getCorrectAnswer().toString() +
                ", category=" + getCategory() +
                ", imageUrl=" + imageUrl +
                '}';
    }

    @Override
    public JsonNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode
                obj = mapper.createObjectNode()
                    .put("id", getId())
                    .put("statement", getStatement())
                    .put("imageUrl", imageUrl);
                obj .put("category", getCategory().toJson());
        ArrayNode optionsArray = mapper.createArrayNode();
        getOptions().forEach(optionImage -> optionsArray.add(optionImage.toJson()));
        obj         .put("options", optionsArray);
        return obj;
    }
}
