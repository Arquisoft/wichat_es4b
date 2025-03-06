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
public class ImageQuestion implements JsonEntity {
    public static final String ENGLISH = "en";
    public static final String SPANISH = "es";
    public static final String FRENCH = "fr";
    public static final String DEUCH = "de";

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = false)
    private String statement;

    @OneToMany(mappedBy = "imageQuestion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Answer> options = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Answer correctAnswer;

    @ManyToOne
    private Category category;

    private String language;

    @Column(unique = false)
    private String imageUrl;

    public ImageQuestion(String statement, List<Answer> options, Category category, String language, String imageUrl) {
        this.statement = statement;
        this.language = language;
        this.imageUrl = imageUrl;
        Associations.ImageQuestionAnswers.addAnswer(this, options);
        Associations.ImageQuestionCategory.addCategory(this, category);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageQuestion that = (ImageQuestion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ImageQuestion{" +
                "statement='" + statement + '\'' +
                ", options=" + options.toString() +
                ", correctAnswer=" + correctAnswer.toString() +
                ", category=" + category +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    @Override
    public JsonNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode()
                .put("id", id)
                .put("statement", statement)
                .put("imageUrl", imageUrl); // Añadimos la URL de la imagen
            obj.put("category", category.toJson());

        ArrayNode optionsArray = mapper.createArrayNode();
        options.forEach(option -> optionsArray.add(option.toJson()));
        obj.put("options", optionsArray);

        return obj;
    }

    public boolean hasEmptyOptions() {
        for (Answer a : options) {
            if (a.getText().isEmpty() || a.getText().isBlank() || a.getText() == null) {
                return true;
            }
        }
        return false;
    }
}
