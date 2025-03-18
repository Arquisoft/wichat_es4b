package com.uniovi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ApiKeyImage {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String keyToken = UUID.randomUUID().toString();

    @OneToOne
    private PlayerImage player;

    @OneToMany(mappedBy = "apiKey", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RestApiAccessLogImage> accessLogs = new HashSet<>();
}
