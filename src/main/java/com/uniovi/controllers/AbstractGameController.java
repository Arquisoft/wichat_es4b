package com.uniovi.controllers;

import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.Player;
import com.uniovi.entities.abstracts.AbstractGameSession;
import com.uniovi.entities.abstracts.AbstractQuestion;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.PlayerService;
import com.uniovi.services.QuestionService;
import com.uniovi.services.abstracts.AbstractMultiplayerSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public abstract class AbstractGameController<T extends AbstractQuestion<?>, Q extends AbstractGameSession<T>, E extends QuestionDto> {

	private static final String GAMESESSION_STR = "gameSession";

	protected final String gamesessionStr;

	protected final PlayerService playerService;
	protected final AbstractMultiplayerSessionService<T> multiplayerSessionService;
	protected final GameSessionService<Q> gameSessionService;
	protected final QuestionService<T, E> questionService;

	/*
	 * Constructores Services
	 */

	public AbstractGameController(PlayerService playerService, AbstractMultiplayerSessionService<T> multiplayerSessionService, GameSessionService<Q> gameSessionService, QuestionService<T, E> questionService, String gamesessionStr) {
		this.playerService = playerService;
		this.multiplayerSessionService = multiplayerSessionService;
		this.gameSessionService = gameSessionService;
		this.questionService = questionService;

		this.gamesessionStr = gamesessionStr;
	}

	public AbstractGameController(PlayerService playerService, AbstractMultiplayerSessionService<T> multiplayerSessionService, GameSessionService<Q> gameSessionService, QuestionService<T, E> questionService) {
		this(playerService, multiplayerSessionService, gameSessionService, questionService, GAMESESSION_STR);
	}

	/*
	 * Metodos a redefinir HTTP
	 */

	public String createMultiplayerGame(HttpSession session, Principal principal) {
		Optional<Player> player = playerService.getUserByUsername(principal.getName());
		if (player.isEmpty()) {
			// Handle the case where the player is not found
			return "redirect:/";
		}
		Player p = player.get();
		String code = "" + playerService.createMultiplayerGame(p.getId());
		multiplayerSessionService.multiCreate(code, p.getId());
		session.setAttribute("multiplayerCode", code);
		return "redirect:/game/lobby";
	}

	public Map<String, String> endMultiplayerGameTable(@PathVariable String code) {
		Map<Player, Integer> playerScores = multiplayerSessionService.getPlayersWithScores(Integer.parseInt(code));
		Map<String, String> playersNameWithScore = new HashMap<>();
		for (Map.Entry<Player, Integer> player : playerScores.entrySet()) {
			String playerName = player.getKey().getUsername();
			String playerScoreValue;
			if (player.getValue() == -1) {
				playerScoreValue = "N/A";
			} else {
				playerScoreValue = "" + player.getValue();
			}
			playersNameWithScore.put(playerName, playerScoreValue);
		}
		return playersNameWithScore;
	}

	public List<String> updatePlayerList(@PathVariable String code) {
		Map<Player, Integer> players = multiplayerSessionService.getPlayersWithScores(Integer.parseInt(code));
		List<String> playerNames = new ArrayList<>();
		for (Map.Entry<Player, Integer> player : players.entrySet()) {
			playerNames.add(player.getKey().getUsername());
		}
		Collections.sort(playerNames);
		return playerNames;
	}





	/*
	 * Metodos adicionales
	 */

	protected void checkUpdateGameSession(AbstractGameSession<T> gameSession, Long idQuestion, Long idAnswer) {
		if (!gameSession.isAnswered(gameSession.getCurrentQuestion())) {
			gameSession.addAnsweredQuestion(gameSession.getCurrentQuestion());

			if (idAnswer == -1 || timeOut(gameSession) || !questionService.checkAnswer(idQuestion, idAnswer)) {
				gameSession.addQuestion(false, 0);
			} else {
				gameSession.addQuestion(true, getRemainingTime(gameSession));
			}
		}
	}

	protected final String getGamesessionStr() {
		return gamesessionStr;
	}

	protected Q getSessionAttribute(HttpSession session) {
		return (Q) session.getAttribute(getGamesessionStr());
	}

	protected void addQuestionAttributes(Model model, Q gameSession) {
		model.addAttribute("question", gameSession.getCurrentQuestion());
		model.addAttribute("questionDuration", getRemainingTime(gameSession));
	}


	protected Player getLoggedInPlayer(Principal principal) {
		Optional<Player> player = playerService.getUserByUsername(principal.getName());
		return player.orElse(null);
	}

	protected int getRemainingTime(AbstractGameSession<?> gameSession) {
		return (int) Duration.between(LocalDateTime.now(), gameSession.getFinishTime().plusSeconds(questionService.getSecondsPerQuestion())).toSeconds();
	}

	protected boolean timeOut(AbstractGameSession<?> gameSession) {
		return getRemainingTime(gameSession) <= 0;
	}

	protected boolean checkUpdateGameSession(Q gameSession, HttpSession session) {
		// if time since last question started is greater than the time per question, add a new question (or check for game finish)
		if (timeOut(gameSession) && gameSession.getQuestionsToAnswer().isEmpty() && gameSession.getCurrentQuestion() != null) {
			gameSession.addQuestion(false, 0);
			gameSession.addAnsweredQuestion(gameSession.getCurrentQuestion());
			if (gameSession.getQuestionsToAnswer().isEmpty()) {
				gameSessionService.endGame(gameSession);
				session.removeAttribute(getGamesessionStr());
				return true;
			}
		}
		return false;
	}
}
