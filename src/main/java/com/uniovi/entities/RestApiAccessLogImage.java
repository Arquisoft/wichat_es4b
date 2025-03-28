package com.uniovi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestApiAccessLogImage {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private ApiKeyImage apiKey;

    private String path;

    @Column(columnDefinition = "VARCHAR(10000)")
    private String details;
}
