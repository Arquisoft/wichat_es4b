package com.uniovi.entities.abstracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniovi.entities.Player;
import com.uniovi.interfaces.JsonEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
public abstract class AbstractGameSession<T extends AbstractQuestion<?>> implements JsonEntity, Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Player player;

    private Integer correctQuestions;
    private Integer totalQuestions;

    // When game started
    private LocalDateTime createdAt;

    // When the last question started, or when the game ended
    private LocalDateTime finishTime;

    private int score;

    @Transient
    private Set<T> answeredQuestions = new HashSet<>();

    @Transient
    private List<T> questionsToAnswer = new ArrayList<>();

    @Transient
    private T currentQuestion;

    @Transient
    private boolean isMultiplayer = false;

    @Transient
    private boolean isFinished = false;

    public AbstractGameSession(Player player, List<T> questions) {
        this.player = player;
        this.questionsToAnswer = questions;
        this.createdAt = LocalDateTime.now();
        this.finishTime = LocalDateTime.now();
        this.correctQuestions = 0;
        this.totalQuestions = 0;
        getNextQuestion();
    }

    public void addQuestion(boolean correct, int timeLeft) {
        if(correct)
            correctQuestions++;
        totalQuestions++;

        if (correct) {
            score += timeLeft + 10 /* magic number TBD */;
        }
    }

    public void addAnsweredQuestion(T question) {
        questionsToAnswer.remove(question);
        answeredQuestions.add(question);
    }

    public boolean isAnswered(T question) {
        return answeredQuestions.contains(question);
    }

    public T getNextQuestion() {
        if (questionsToAnswer.isEmpty()) {
            currentQuestion = null;
            return null;
        }
        Collections.shuffle(questionsToAnswer);
        currentQuestion = questionsToAnswer.get(0);
        return currentQuestion;
    }

    @Override
    public JsonNode toJson() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.createObjectNode()
                .put("id", id)
                .put("player", player.getId())
                .put("correctQuestions", correctQuestions)
                .put("totalQuestions", totalQuestions)
                .put("createdAt", createdAt.toString())
                .put("finishTime", finishTime.toString())
                .put("score", score);
    }

	public boolean hasQuestionId(Long idQuestion) {
		for (T q : questionsToAnswer)
			if (q.getId().equals(idQuestion))
				return true;

		for (T q : answeredQuestions)
			if (q.getId().equals(idQuestion))
				return true;
		return false;
	}

    public String getDuration() {
        if (createdAt != null && finishTime != null) {
            Duration duration = Duration.between(createdAt, finishTime);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return "00:00:00";
        }
    }
}
