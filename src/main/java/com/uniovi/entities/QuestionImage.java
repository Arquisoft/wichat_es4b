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
public class QuestionImage implements JsonEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = false)
    private String statement;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AnswerImage> options = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private AnswerImage correctAnswer;

    @ManyToOne
    private CategoryImage category;

    private String language;

    private String imageUrl;

    public QuestionImage(String statement, List<AnswerImage> optionsImage, AnswerImage correctAnswerImage, CategoryImage categoryImage, String language, String imageUrl) {
        Assert.isTrue(optionsImage.contains(correctAnswerImage), "Correct answer must be one of the options");
        this.statement = statement;
        Associations.QuestionImageAnswers.addAnswer(this, optionsImage);
        this.correctAnswer = correctAnswerImage;
        this.category = categoryImage;
        this.language = language;
        this.imageUrl = imageUrl;
    }

    public QuestionImage(String statement, List<AnswerImage> optionsImage, AnswerImage correctAnswerImage, CategoryImage categoryImage, Language language,String imageUrl) {
        Assert.isTrue(optionsImage.contains(correctAnswerImage), "Correct answer must be one of the options");
        this.statement = statement;
        Associations.QuestionImageAnswers.addAnswer(this, optionsImage);
        this.correctAnswer = correctAnswerImage;
        this.category = categoryImage;
        this.language = language.getCode();
        this.imageUrl = imageUrl;
    }

    public void addOption(AnswerImage optionImage) {
        options.add(optionImage);
    }

    public void removeOption(AnswerImage optionImage){
        options.remove(optionImage);
    }

    public AnswerImage getOption(int index){
        return options.get(index);
    }

    public AnswerImage getOptions(String answerImage){
        for (AnswerImage optionImage : options) {
            if (optionImage.getText().equals(answerImage)) {
                return optionImage;
            }
        }
        return null;
    }

    public boolean isCorrectAnswer(AnswerImage answerImage){
        return answerImage.isCorrect();
    }

    public List<AnswerImage> returnScrambledOptions(){
        Collections.shuffle(options);
        return options;
    }

    public boolean hasEmptyOptions() {
        for (AnswerImage a : options) {
            if (a.getText().isEmpty() || a.getText().isBlank() || a.getText() == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionImage questionImage = (QuestionImage) o;
        return Objects.equals(id, questionImage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "QuestionImage{" +
                "statement='" + statement + '\'' +
                ", options=" + options.toString() +
                ", correctAnswer=" + correctAnswer.toString() +
                ", category=" + category +
                ", imageUrl=" + imageUrl +
                '}';
    }

    @Override
    public JsonNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode
                obj = mapper.createObjectNode()
                    .put("id", id)
                    .put("statement", statement)
                    .put("imageUrl", imageUrl);
                obj .put("category", category.toJson());
        ArrayNode optionsArray = mapper.createArrayNode();
        options.forEach(optionImage -> optionsArray.add(optionImage.toJson()));
        obj         .put("options", optionsArray);
        return obj;
    }
}
