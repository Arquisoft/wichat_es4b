package com.uniovi.components.generators;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.entities.Category;
import com.uniovi.entities.abstracts.AbstractQuestion;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public interface QuestionGenerator<T extends AbstractQuestion> {
    List<T> getQuestions(String language) throws IOException, InterruptedException;

    List<T> getQuestions(String language, JsonNode question, Category cat) throws IOException, InterruptedException;
}
