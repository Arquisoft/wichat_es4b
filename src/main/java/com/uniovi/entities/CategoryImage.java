package com.uniovi.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniovi.interfaces.JsonEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class CategoryImage implements JsonEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String name;
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
    private Set<QuestionImage> questions = new HashSet<>();

    public CategoryImage(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CategoryImage(String categoryName) {
        this.name = categoryName;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public JsonNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.createObjectNode()
                .put("id", id)
                .put("name", name)
                .put("description", description);
    }
}
