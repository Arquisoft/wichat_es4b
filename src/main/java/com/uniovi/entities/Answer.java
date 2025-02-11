package com.uniovi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.interfaces.JsonEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Answer implements JsonEntity {

    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    private String text;

    @JsonIgnore
    private boolean correct;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    private Question question;

    public Answer(String text, boolean correct) {
        this.text = text;
        this.correct = correct;
    }

    public String toString() {
        return text;
    }

    @Override
    public JsonNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();
        obj     .put("id", id)
                .put("text", text)
                .put("correct", correct)
                .put("question", question.getId());
        return obj;
    }
}