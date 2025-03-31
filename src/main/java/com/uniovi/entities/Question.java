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
public class Question extends QuestionBase {

    public Question(String statement, List<Answer> options, Answer correctAnswer, Category category, String language) {
        super(statement, options, correctAnswer, category, language);
    }

    public Question(String statement, List<Answer> options, Answer correctAnswer, Category category, Language language) {
        super(statement, options, correctAnswer, category, language);
    }

}
