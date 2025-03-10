package com.uniovi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter // getters para todas las propiedades
@Setter // setters para todas las propiedades
@Entity
public class MultiplayerSessionImage {
    @Id
    @GeneratedValue
    private Long id;
    @Column
    private String multiplayerCode;

    @ElementCollection
    @Column
    private Map<Player, Integer> playerScores = new HashMap<>();

    public MultiplayerSessionImage() {}

    public MultiplayerSessionImage(String code, Player p) {
        this.multiplayerCode=code;
        playerScores.put(p,-1);
    }

    public void addPlayer(Player p){
        playerScores.put(p,-1);
    }
}
