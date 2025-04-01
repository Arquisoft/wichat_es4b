package com.uniovi.entities.abstracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.entities.Category;
import com.uniovi.entities.Language;
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
@NoArgsConstructor
@MappedSuperclass
public abstract class AbstractQuestion<T extends AbstractAnswer> implements JsonEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String statement;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<T> options = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private T correctAnswer;

    @ManyToOne
    private Category category;

    private String language;

    public AbstractQuestion(String statement, List<T> options, T correctAnswer, Category category, String language) {
        Assert.isTrue(options.contains(correctAnswer), "Correct answer must be one of the options");
        this.statement = statement;
        doOptionsAssociation(options);
        this.correctAnswer = correctAnswer;
        this.category = category;
        this.language = language;
    }

    public AbstractQuestion(String statement, List<T> options, T correctAnswer, Category category, Language language) {
        this(statement, options, correctAnswer, category, language.getCode());
    }

    protected abstract void doOptionsAssociation(List<T> options);

    public void addOption(T option) {
        options.add(option);
    }

    public void removeOption(T option) {
        options.remove(option);
    }

    public T getOption(int index) {
        return options.get(index);
    }

    public T getOptions(String answer) {
        return options.stream().filter(option -> option.getText().equals(answer)).findFirst().orElse(null);
    }

    public boolean isCorrectAnswer(T answer) {
        return answer.isCorrect();
    }

    public List<T> returnScrambledOptions() {
        Collections.shuffle(options);
        return options;
    }

    public boolean hasEmptyOptions() {
        return options.stream().anyMatch(a -> a.getText() == null || a.getText().isBlank());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractQuestion<?> that = (AbstractQuestion<?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AbstractQuestion{" +
                "statement='" + statement + '\'' +
                ", options=" + options.toString() +
                ", correctAnswer=" + correctAnswer.toString() +
                ", category=" + category +
                '}';
    }

    @Override
    public JsonNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode()
                .put("id", id)
                .put("statement", statement);
        obj.put("category", category.toJson());
        ArrayNode optionsArray = mapper.createArrayNode();
        options.forEach(option -> optionsArray.add(option.toJson()));
        obj.put("options", optionsArray);
        return obj;
    }
}