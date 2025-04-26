package com.uniovi.controllers;

import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.GameSessionImage;
import com.uniovi.entities.QuestionImage;
import com.uniovi.services.impl.GameSessionImageServiceImpl;
import com.uniovi.services.impl.MultiplayerSessionImageServiceImpl;
import com.uniovi.services.impl.PlayerServiceImpl;
import com.uniovi.services.impl.QuestionImageServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class GameImageController extends AbstractGameController<QuestionImage, GameSessionImage, QuestionImageDto> {

	private static final String GAMESESSION_STR = "gameSessionImage";


	public GameImageController(QuestionImageServiceImpl questionService, GameSessionImageServiceImpl gameSessionService, PlayerServiceImpl playerService, MultiplayerSessionImageServiceImpl multiplayerSessionService) {
		super(playerService, multiplayerSessionService, gameSessionService, questionService, GAMESESSION_STR);
	}

	/**
	 * This method is used to get the game view and to start the game
	 *
	 * @param model The model to be used
	 * @return The view to be shown
	 */
	@RequestMapping("/game/image")
	public String getGame(HttpSession session, Model model, Principal principal) {
		GameSessionImage gameSession = getSessionAttribute(session);
		if (gameSession != null && !gameSession.isFinished() && !gameSession.isMultiplayer()) {
			if (checkUpdateGameSession(gameSession, session)) {
				return "game/image/fragments/gameFinished";
			}
		} else {
			gameSession = gameSessionService.startNewGame(getLoggedInPlayer(principal));
			session.setAttribute(getGamesessionStr(), gameSession);
			playerService.deleteMultiplayerCode(gameSession.getPlayer().getId());
		}

		addQuestionAttributes(model, gameSession);
		return "game/image/game";
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
	@RequestMapping("/game/image/{idQuestion}/{idAnswer}")
	public String getCheckResult(@PathVariable Long idQuestion, @PathVariable Long idAnswer, Model model, HttpSession session, Principal principal) {
		GameSessionImage gameSession = getSessionAttribute(session);
		if (gameSession == null) {
			return "redirect:/game/image/game";
		}

		if (!gameSession.hasQuestionId(idQuestion)) {
			model.addAttribute("score", gameSession.getScore());
			session.removeAttribute(getGamesessionStr());
			return "redirect:/game/image/game"; // if someone wants to exploit the game, just redirect to the game page
		}

		checkUpdateGameSession(gameSession, idQuestion, idAnswer);

		session.setAttribute("hasJustAnsweredImage", true);
		gameSession.getNextQuestion();
		return updateGame(model, session, principal);
	}

	@RequestMapping("/game/image/update")
	public String updateGame(Model model, HttpSession session, Principal principal) {
		GameSessionImage gameSession = getSessionAttribute(session);
		QuestionImage nextQuestionImage = gameSession.getCurrentQuestion();
		if (nextQuestionImage == null) {
			if (gameSession.isMultiplayer()) {
				//Multiplayer version
				//return playMultiplayerGame(model, session, principal, gameSession);
				throw new IllegalStateException("Multiplayer mode not actved.");
			} else {
				if (!gameSession.isFinished()) {
					gameSessionService.endGame(gameSession);
					gameSession.setFinished(true);
				} else {
					session.removeAttribute(getGamesessionStr());
					model.addAttribute("score", gameSession.getScore());
				}
				return "game/image/fragments/gameFinished";
			}
		}

		if (session.getAttribute("hasJustAnsweredImage") != null) {
			if ((boolean) session.getAttribute("hasJustAnsweredImage")) gameSession.setFinishTime(LocalDateTime.now());
			session.removeAttribute("hasJustAnsweredImage");
		}
		addQuestionAttributes(model, gameSession);
		return "game/image/fragments/gameFrame";
	}

	@RequestMapping("/game/image/points")
	@ResponseBody
	public String getPoints(HttpSession session) {
		GameSessionImage gameSession = getSessionAttribute(session);
		if (gameSession != null) return String.valueOf(gameSession.getScore());
		else return "0";
	}

	@RequestMapping("/game/image/currentQuestion")
	@ResponseBody
	public String getCurrentQuestion(HttpSession session) {
		GameSessionImage gameSession = getSessionAttribute(session);
		if (gameSession != null) {
			return String.valueOf(Math.min(gameSession.getAnsweredQuestions().size() + 1, GameSessionImageServiceImpl.NORMAL_GAME_QUESTION_NUM));
		} else return "0";
	}

	@RequestMapping("/game/image/hint/{id}/{llm}")
	@ResponseBody
	public String getImageQuestionHint(@PathVariable Long id, @PathVariable String llm) {
		Optional<QuestionImage> questionOpt = questionService.getQuestion(id);
		if (questionOpt.isPresent()) {
			QuestionImage question = questionOpt.get();
			return ((QuestionImageServiceImpl) questionService).getHintForImageQuestion(question, llm); // Devuelve solo la pista como String
		}
		return "No se encontró ninguna pista para esta pregunta.";
	}

	/*
	 * MODO MULTIJUGADOR
	 */

//	private String playMultiplayerGame(Model model, HttpSession session, Principal principal, GameSessionImage gameSession) {
//		int code = Integer.parseInt((String) session.getAttribute("multiplayerCodeImage"));
//		List<Player> players = playerService.getUsersByMultiplayerCode(code);
//
//		if (!gameSession.isFinished()) {
//			gameSessionService.endGame(gameSession);
//
//			model.addAttribute("playersImage", players);
//			model.addAttribute("codeImage", session.getAttribute("multiplayerCodeImage"));
//			gameSession.setFinished(true);
//
//			Optional<Player> player = playerService.getUserByUsername(principal.getName());
//			Player p = player.orElse(null);
//			playerService.setScoreMultiplayerCode(p.getId(), "" + gameSession.getScore());
//			multiplayerSessionService.changeScore(p.getMultiplayerCode() + "", p.getId(), gameSession.getScore());
//		} else {
//			model.addAttribute("playersImage", players);
//
//		}
//
//		model.addAttribute("codeImage", session.getAttribute("multiplayerCodeImage"));
//		return "game/image/ranking/multiplayerRanking";
//	}
//
//	@RequestMapping("/image/multiplayerGame/endGame/{code}")
//	public String endMultiplayerGame(Model model, @PathVariable String code) {
//		model.addAttribute("codeImage", code);
//		return "ranking/image/multiplayerRanking";
//	}
//
//	@RequestMapping("/image/multiplayerGame/createGame")
//	@Override
//	public String createMultiplayerGame(HttpSession session, Principal principal) {
//		return super.createMultiplayerGame(session, principal);
//	}
//
//	@RequestMapping("/image/startMultiplayerGame")
//	public String startMultiplayerGame(HttpSession session, Model model, Principal principal) {
//		GameSessionImage gameSession = getSessionAttribute(session);
//		if (gameSession != null) {
//			if (!gameSession.isMultiplayer()) {
//				session.removeAttribute(getGamesessionStr());
//				return "redirect:/image/startMultiplayerGame";
//			}
//
//			if (gameSession.isFinished()) {
//				model.addAttribute("codeImage", session.getAttribute("multiplayerCodeImage"));
//				return "game/image/multiplayerFinished";
//			}
//
//			if (checkUpdateGameSession(gameSession, session)) {
//				return "game/image/fragments/gameFinished";
//			}
//		} else {
//			Optional<Player> player = playerService.getUserByUsername(principal.getName());
//			if (!player.isPresent()) {
//				return "redirect:/";
//			}
//			gameSession = gameSessionService.startNewMultiplayerGame(getLoggedInPlayer(principal), player.get().getMultiplayerCode());
//			if (gameSession == null) return "redirect:/image/multiplayerGame";
//			session.setAttribute(getGamesessionStr(), gameSession);
//		}
//		addQuestionAttributes(model, gameSession);
//		return "game/image/game";
//	}
//
//	@RequestMapping("/game/image/multiplayer")
//	public String getImageMultiplayerGame() {
//		return "redirect:/image/multiplayerGame/createGame";
//	}
//
//
//	@RequestMapping("/image/multiplayerGame/{code}")
//	public String joinMultiplayerGame(@PathVariable String code, HttpSession session, Principal principal, Model model) {
//		if (!multiplayerSessionService.existsCode(code)) {
//			model.addAttribute("errorKeyImage", "multi.code.invalid");
//			return "game/image/multiplayerGame";
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
//			model.addAttribute("multiplayerGameCodeImage", code);
//			session.setAttribute("multiplayerCodeImage", code);
//			return "redirect:/game/image/lobby";
//		} else {
//			return "redirect:/image/multiplayerGame";
//		}
//	}
//
//	@RequestMapping("/image/endGameList/{code}")
//	@ResponseBody
//	@Override
//	public Map<String, String> endMultiplayerGameTable(@PathVariable String code) {
//		return super.endMultiplayerGameTable(code);
//	}
//
//	@RequestMapping("/game/image/lobby/{code}")
//	@ResponseBody
//	@Override
//	public List<String> updatePlayerList(@PathVariable String code) {
//		return super.updatePlayerList(code);
//	}
//
//	@RequestMapping("/game/image/lobby")
//	public String createLobby(HttpSession session, Model model) {
//		int code = Integer.parseInt((String) session.getAttribute("multiplayerCodeImage"));
//		List<Player> players = playerService.getUsersByMultiplayerCode(code);
//		model.addAttribute("playersImage", players);
//		model.addAttribute("codeImage", session.getAttribute("multiplayerCodeImage"));
//		return "game/image/lobby";
//	}
}
