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
public class GameSessionImage extends AbstractGameSession<QuestionImage> {

    public GameSessionImage(Player player, List<QuestionImage> questionsImage) {
        super(player, questionsImage);
    }
}

