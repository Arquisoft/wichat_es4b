package com.uniovi.components.generators;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.entities.Category;
import com.uniovi.entities.CategoryImage;
import com.uniovi.entities.Question;
import com.uniovi.entities.QuestionImage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public interface QuestionImageGenerator {
    List<QuestionImage> getQuestions(String language) throws IOException, InterruptedException;

    List<QuestionImage> getQuestions(String language, JsonNode questionImage, CategoryImage cat) throws IOException, InterruptedException;
}
