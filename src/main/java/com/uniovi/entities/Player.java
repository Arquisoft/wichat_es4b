package com.uniovi.entities;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.interfaces.JsonEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter // getters para todas las propiedades
@Setter // setters para todas las propiedades
@NoArgsConstructor(access = AccessLevel.PROTECTED) // constructor sin argumentos
@Entity
public class Player implements JsonEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    @NotEmpty
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @NotEmpty
    private String password;

    @Column
    private Integer multiplayerCode;

    @Column
    private String scoreMultiplayerCode;


    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<GameSession> gameSessions = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "player")
    private ApiKey apiKey;

    //*****  IMAGE

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<GameSessionImage> gameSessionsImage = new HashSet<>();

    //*****




    // Transient: no se almacena en la base de datos
    @Transient
    private String passwordConfirm;

    public Player(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public JsonNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode rolesArray = mapper.createArrayNode();
        roles.forEach(role -> rolesArray.add(role.getName()));

        ArrayNode gameSessionsArray = mapper.createArrayNode();
        gameSessions.forEach(gameSession -> gameSessionsArray.add(gameSession.toJson()));

        ObjectNode obj = mapper.createObjectNode();
        obj     .put("id", id)
                .put("username", username)
                .put("email", email)
                .put("roles", rolesArray);
        obj     .put("gameSessions", gameSessionsArray);
        return obj;
    }
}
