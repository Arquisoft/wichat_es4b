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
public class QuestionBase implements JsonEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = false)
    private String statement;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Answer> options = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Answer correctAnswer;

    @ManyToOne
    private Category category;

    private String language;

    public QuestionBase(String statement, List<Answer> options, Answer correctAnswer, Category category, String language) {
        Assert.isTrue(options.contains(correctAnswer), "Correct answer must be one of the options");
        this.statement = statement;
        Associations.QuestionAnswers.addAnswer(this, options);
        this.correctAnswer = correctAnswer;
        this.category = category;
        this.language = language;

    }

    public QuestionBase(String statement, List<Answer> options, Answer correctAnswer, Category category, Language language) {
        Assert.isTrue(options.contains(correctAnswer), "Correct answer must be one of the options");
        this.statement = statement;
        Associations.QuestionAnswers.addAnswer(this, options);
        this.correctAnswer = correctAnswer;
        this.category = category;
        this.language = language.getCode();
    }

    public void addOption(Answer option) {
        options.add(option);
    }

    public void removeOption(Answer option){
        options.remove(option);
    }

    public Answer getOption(int index){
        return options.get(index);
    }

    public Answer getOptions(String answer){
        for (Answer option : options) {
            if (option.getText().equals(answer)) {
                return option;
            }
        }
        return null;
    }

    public boolean isCorrectAnswer(Answer answer){
        return answer.isCorrect();
    }

    public List<Answer> returnScrambledOptions(){
        Collections.shuffle(options);
        return options;
    }

    public boolean hasEmptyOptions() {
        for (Answer a : options) {
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
        QuestionBase question = (QuestionBase) o;
        return Objects.equals(id, question.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Question{" +
                "statement='" + statement + '\'' +
                ", options=" + options.toString() +
                ", correctAnswer=" + correctAnswer.toString() +
                ", category=" + category +
                '}';
    }

    @Override
    public JsonNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode
                obj = mapper.createObjectNode()
                    .put("id", id)
                    .put("statement", statement);
                obj .put("category", category.toJson());
        ArrayNode optionsArray = mapper.createArrayNode();
        options.forEach(option -> optionsArray.add(option.toJson()));
        obj         .put("options", optionsArray);
        return obj;
    }
}
