package com.uniovi.entities;

import com.uniovi.entities.abstracts.AbstractGameSession;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class GameSession extends AbstractGameSession<Question> {

    public GameSession(Player player, List<Question> questions) {
        super(player, questions);
    }
}
