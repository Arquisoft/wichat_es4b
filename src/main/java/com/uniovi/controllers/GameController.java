package com.uniovi.controllers;

import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.GameSession;
import com.uniovi.entities.Question;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.PlayerService;
import com.uniovi.services.impl.GameSessionServiceImpl;
import com.uniovi.services.impl.MultiplayerSessionServiceImpl;
import com.uniovi.services.impl.QuestionServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class GameController extends AbstractGameController<Question, GameSession, QuestionDto> {


	public GameController(QuestionServiceImpl questionService, GameSessionServiceImpl gameSessionService,
						  PlayerService playerService, MultiplayerSessionServiceImpl multiplayerSessionService) {
		super(playerService, multiplayerSessionService, gameSessionService, questionService);
	}


	/**
	 * This method is used to get the game view and to start the game
	 *
	 * @param model The model to be used
	 * @return The view to be shown
	 */
	@RequestMapping("/game/pregunta")
	public String getGame(HttpSession session, Model model, Principal principal) {
		GameSession gameSession = getSessionAttribute(session);
		if (gameSession != null && !gameSession.isFinished() && !gameSession.isMultiplayer()) {
			if (checkUpdateGameSession(gameSession, session)) {
				return "game/fragments/gameFinished";
			}
		} else {
			gameSession = gameSessionService.startNewGame(getLoggedInPlayer(principal));
			session.setAttribute(getGamesessionStr(), gameSession);
			playerService.deleteMultiplayerCode(gameSession.getPlayer().getId());
		}
		addQuestionAttributes(model, gameSession);
		return "game/GamePreguntaCaliente";
	}

	/**
	 * This method is used to check the answer for a specific question
	 *
	 * @param idQuestion The id of the question.
	 * @param idAnswer   The id of the answer. If the id is -1, it means that the user has not selected any answer and the
	 *                   time has run out.
	 * @param model      The model to be used.
	 * @return The view to be shown, if the answer is correct, the success view is shown, otherwise the failure view is
	 * shown or the timeOutFailure view is shown.
	 */
	@RequestMapping("/game/{idQuestion}/{idAnswer}")
	public String getCheckResult(@PathVariable Long idQuestion, @PathVariable Long idAnswer, Model model, HttpSession session, Principal principal) {
		GameSession gameSession = getSessionAttribute(session);
		if (gameSession == null) {
			return "redirect:/game/pregunta";
		}

		if (!gameSession.hasQuestionId(idQuestion)) {
			model.addAttribute("score", gameSession.getCorrectQuestions());
			session.removeAttribute(getGamesessionStr());
			return "redirect:/game/pregunta"; // if someone wants to exploit the game, just redirect to the game page
		}

		checkUpdateGameSession(gameSession, idQuestion, idAnswer);

		session.setAttribute("hasJustAnswered", true);
		gameSession.getNextQuestion();
		return updateGame(model, session, principal);
	}

	@RequestMapping("/game/update")
	public String updateGame(Model model, HttpSession session, Principal principal) {
		GameSession gameSession = getSessionAttribute(session);
		Question nextQuestion = gameSession.getCurrentQuestion();
		if (nextQuestion == null)
			if (gameSession.isMultiplayer()) {
				// Multiplayer game
				//return playMultiplayer(model, session, principal, gameSession);
				throw new IllegalStateException("Multiplayer game not implemented");
			} else {
				if (!gameSession.isFinished()) {
					gameSessionService.endGame(gameSession);
					gameSession.setFinished(true);
				} else {
					session.removeAttribute(getGamesessionStr());
					model.addAttribute("score", gameSession.getCorrectQuestions());
				}
				return "game/fragments/gameFinished";
			}

		int preguntasRespondidas = gameSession.getAnsweredQuestions().size();
		int preguntasCorrectas = gameSession.getCorrectQuestions();
		if (preguntasCorrectas - preguntasRespondidas != 0) {
			gameSessionService.endGame(gameSession);
			gameSession.setFinished(true);
			model.addAttribute("score", gameSession.getCorrectQuestions());
			return "game/fragments/gameFinished";
		}

		if (session.getAttribute("hasJustAnswered") != null) {
			if ((boolean) session.getAttribute("hasJustAnswered")) gameSession.setFinishTime(LocalDateTime.now());
			session.removeAttribute("hasJustAnswered");
		}
		addQuestionAttributes(model, gameSession);
		return "game/fragments/gameFramePreguntaCaliente";
	}

	@RequestMapping("/game/points")
	@ResponseBody
	public String getPoints(HttpSession session) {
		GameSession gameSession = getSessionAttribute(session);
		if (gameSession != null) return String.valueOf(gameSession.getCorrectQuestions());
		else return "0";
	}

	@RequestMapping("/game/currentQuestion")
	@ResponseBody
	public String getCurrentQuestion(HttpSession session) {
		GameSession gameSession = getSessionAttribute(session);
		if (gameSession != null)
			return String.valueOf(Math.min(gameSession.getAnsweredQuestions().size() + 1, GameSessionService.NORMAL_GAME_QUESTION_NUM));
		else return "0";
	}


	/*
	 * MODO MULTIJUGADOR
	 */

//	@RequestMapping("/game/trivial/multiplayer")
//	public String getTrivialMultiplayerGame() {
//		return "redirect:/multiplayerGame/createGame";
//	}
//
//	@RequestMapping("/multiplayerGame/{code}")
//	public String joinMultiplayerGame(@PathVariable String code, HttpSession session, Principal principal, Model
//			model) {
//		if (!multiplayerSessionService.existsCode(code)) {
//			model.addAttribute("errorKey", "multi.code.invalid");
//			return "game/multiplayerGame";
//		}
//
//		Optional<Player> player = playerService.getUserByUsername(principal.getName());
//		if (player.isEmpty()) {
//			// Handle the case where the player is not found
//			return "redirect:/";
//		}
//		Player p = player.get();
//		if (playerService.changeMultiplayerCode(p.getId(), code)) {
//			multiplayerSessionService.addToLobby(code, p.getId());
//			model.addAttribute("multiplayerGameCode", code);
//			session.setAttribute("multiplayerCode", code);
//			return "redirect:/game/lobby";
//		} else {
//			return "redirect:/multiplayerGame";
//		}
//	}
//
//	@RequestMapping("/multiplayerGame/createGame")
//	@Override
//	public String createMultiplayerGame(HttpSession session, Principal principal) {
//		return super.createMultiplayerGame(session, principal);
//	}
//
//	@RequestMapping("/startMultiplayerGame")
//	public String startMultiplayerGame(HttpSession session, Model model, Principal principal) {
//		GameSession gameSession = getSessionAttribute(session);
//
//		if (gameSession != null) {
//			if (!gameSession.isMultiplayer()) {
//				session.removeAttribute(getGamesessionStr());
//				return "redirect:/startMultiplayerGame";
//			}
//
//			if (gameSession.isFinished()) {
//				model.addAttribute("code", session.getAttribute("multiplayerCode"));
//				return "game/multiplayerFinished";
//			}
//
//			if (checkUpdateGameSession(gameSession, session)) {
//				return "game/fragments/gameFinished";
//			}
//		} else {
//			Optional<Player> player = playerService.getUserByUsername(principal.getName());
//			if (player.isEmpty()) {
//				return "redirect:/";
//			}
//			gameSession = gameSessionService.startNewMultiplayerGame(getLoggedInPlayer(principal), player.get().getMultiplayerCode());
//			if (gameSession == null) return "redirect:/multiplayerGame";
//			session.setAttribute(getGamesessionStr(), gameSession);
//		}
//		addQuestionAttributes(model, gameSession);
//		return "game/GamePreguntaCaliente";
//	}
//
//	@RequestMapping("/multiplayerGame/endGame/{code}")
//	public String endMultiplayerGame(Model model, @PathVariable String code) {
//		model.addAttribute("code", code);
//		return "ranking/multiplayerRanking";
//	}
//
//	@RequestMapping("/endGameList/{code}")
//	@ResponseBody
//	@Override
//	public Map<String, String> endMultiplayerGameTable(@PathVariable String code) {
//		return super.endMultiplayerGameTable(code);
//	}
//
//	@RequestMapping("/game/lobby/{code}")
//	@ResponseBody
//	@Override
//	public List<String> updatePlayerList(@PathVariable String code) {
//		return super.updatePlayerList(code);
//	}
//
//
//	@RequestMapping("/game/lobby")
//	public String createLobby(HttpSession session, Model model) {
//		int code = Integer.parseInt((String) session.getAttribute("multiplayerCode"));
//		List<Player> players = playerService.getUsersByMultiplayerCode(code);
//		model.addAttribute("players", players);
//		model.addAttribute("code", session.getAttribute("multiplayerCode"));
//		return "game/lobby";
//	}
//
//	private String playMultiplayer(Model model, HttpSession session, Principal principal, GameSession gameSession) {
//		int code = Integer.parseInt((String) session.getAttribute("multiplayerCode"));
//		List<Player> players = playerService.getUsersByMultiplayerCode(code);
//
//		if (!gameSession.isFinished()) {
//			gameSessionService.endGame(gameSession);
//
//			model.addAttribute("players", players);
//			model.addAttribute("code", session.getAttribute("multiplayerCode"));
//			gameSession.setFinished(true);
//
//			Optional<Player> player = playerService.getUserByUsername(principal.getName());
//			if (player.isEmpty()) {
//				// Handle the case where the player is not found
//				return "redirect:/";
//			}
//			Player p = player.get();
//			playerService.setScoreMultiplayerCode(p.getId(), "" + gameSession.getScore());
//			multiplayerSessionService.changeScore(p.getMultiplayerCode() + "", p.getId(), gameSession.getScore());
//		} else {
//			model.addAttribute("players", players);
//
//		}
//
//		model.addAttribute("code", session.getAttribute("multiplayerCode"));
//		return "ranking/multiplayerRanking";
//	}
}
