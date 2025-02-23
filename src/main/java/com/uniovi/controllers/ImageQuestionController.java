package com.uniovi.controllers;

import com.uniovi.entities.GameSession;
import com.uniovi.entities.ImageQuestion;
import com.uniovi.entities.Player;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.ImageQuestionService;
import com.uniovi.services.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class ImageQuestionController {
    private static final String GAMESESSION_STR = "gameSession";
    private final ImageQuestionService imageQuestionService;
    private final GameSessionService gameSessionService;
    private final PlayerService playerService;

    public ImageQuestionController(ImageQuestionService imageQuestionService, GameSessionService gameSessionService,
                                   PlayerService playerService) {
        this.imageQuestionService = imageQuestionService;
        this.gameSessionService = gameSessionService;
        this.playerService = playerService;
    }

    /**
     * This method is used to get the game view and to start the game with image questions
     * @param model The model to be used
     * @return The view to be shown
     */
    @GetMapping("/game")
    public String getGame(HttpSession session, Model model, Principal principal) {
        GameSession gameSession = (GameSession) session.getAttribute(GAMESESSION_STR);
        if (gameSession != null && !gameSession.isFinished()) {
            if (checkUpdateGameSession(gameSession, session)) {
                return "game/fragments/gameFinished";
            }
        } else {
            gameSession = gameSessionService.startNewGame(getLoggedInPlayer(principal));
            session.setAttribute(GAMESESSION_STR, gameSession);
        }

        model.addAttribute("question", gameSession.getCurrentImageQuestion());
        model.addAttribute("questionDuration", getRemainingTime(gameSession));
        return "game/imageGame";
    }

    /**
     * This method checks if the game session needs to be updated
     * @param gameSession The game session to be checked
     * @param session The session to be used
     * @return True if the game session has ended, false otherwise
     */
    private boolean checkUpdateGameSession(GameSession gameSession, HttpSession session) {
        if (getRemainingTime(gameSession) <= 0
                && gameSession.getQuestionsToAnswer().isEmpty()
                && gameSession.getCurrentImageQuestion() != null) {
            gameSession.addQuestion(false, 0);
            gameSession.addAnsweredQuestion(gameSession.getCurrentImageQuestion());
            if (gameSession.getQuestionsToAnswer().isEmpty()) {
                gameSessionService.endGame(gameSession);
                session.removeAttribute(GAMESESSION_STR);
                return true;
            }
        }

        return false;
    }

    private int getRemainingTime(GameSession gameSession) {
        return (int) Duration.between(LocalDateTime.now(),
                gameSession.getFinishTime().plusSeconds(ImageQuestionService.SECONDS_PER_QUESTION)).toSeconds();
    }

    private Player getLoggedInPlayer(Principal principal) {
        Optional<Player> player = playerService.getUserByUsername(principal.getName());
        return player.orElse(null);
    }

    /**
     * This method is used to check the answer for a specific image question
     * @param idQuestion The id of the image question.
     * @param idAnswer The id of the answer. If the id is -1, it means that the user has not selected any answer and the
     *                 time has run out.
     * @param model The model to be used.
     * @return The view to be shown, if the answer is correct, the success view is shown, otherwise the failure view is
     * shown or the timeOutFailure view is shown.
     */
    @GetMapping("/game/{idQuestion}/{idAnswer}")
    public String getCheckResult(@PathVariable Long idQuestion, @PathVariable Long idAnswer, Model model, HttpSession session, Principal principal) {
        GameSession gameSession = (GameSession) session.getAttribute(GAMESESSION_STR);
        if (gameSession == null) {
            return "redirect:/game";
        }

        if (!gameSession.hasQuestionId(idQuestion)) {
            model.addAttribute("score", gameSession.getScore());
            session.removeAttribute(GAMESESSION_STR);
            return "redirect:/game"; // if someone wants to exploit the game, just redirect to the game page
        }

        if (idAnswer == -1 || getRemainingTime(gameSession) <= 0) {
            gameSession.addAnsweredQuestion(gameSession.getCurrentImageQuestion());
            gameSession.addQuestion(false, 0);
        } else if (imageQuestionService.checkAnswer(idQuestion, idAnswer)) {
            if (!gameSession.isAnswered(gameSession.getCurrentImageQuestion())) {
                gameSession.addQuestion(true, getRemainingTime(gameSession));
                gameSession.addAnsweredQuestion(gameSession.getCurrentImageQuestion());
            }
        } else {
            gameSession.addAnsweredQuestion(gameSession.getCurrentImageQuestion());
            gameSession.addQuestion(false, 0);
        }

        session.setAttribute("hasJustAnswered", true);
        gameSession.getNextQuestion();
        return updateGame(model, session, principal);
    }

    @GetMapping("/game/update")
    public String updateGame(Model model, HttpSession session, Principal principal) {
        GameSession gameSession = (GameSession) session.getAttribute(GAMESESSION_STR);
        ImageQuestion nextQuestion = gameSession.getCurrentImageQuestion();
        if (nextQuestion == null) {
            gameSessionService.endGame(gameSession);
            gameSession.setFinished(true);
            session.removeAttribute(GAMESESSION_STR);
            return "game/fragments/gameFinished";
        }

        if (session.getAttribute("hasJustAnswered") != null) {
            if ((boolean) session.getAttribute("hasJustAnswered"))
                gameSession.setFinishTime(LocalDateTime.now());
            session.removeAttribute("hasJustAnswered");
        }
        model.addAttribute("question", gameSession.getCurrentImageQuestion());
        model.addAttribute("questionDuration", getRemainingTime(gameSession));
        return "game/fragments/gameFrame";
    }

    @GetMapping("/game/points")
    @ResponseBody
    public String getPoints(HttpSession session) {
        GameSession gameSession = (GameSession) session.getAttribute(GAMESESSION_STR);
        if (gameSession != null)
            return String.valueOf(gameSession.getScore());
        else
            return "0";
    }


    @GetMapping("/game/image-question/{id}/hint")
    @ResponseBody
    public String getImageQuestionHint(@PathVariable Long id, @RequestParam String playerQuestion) {
        // Intenta obtener la pregunta con imagen
        // README: Hay que devolver la vista del juego de nuevo metiendo la respuesta como atributo  de Model como se viene haciendo
        // en las practicas de sdi. IN_DEVELOPMENT
        return imageQuestionService.getImageQuestion(id)
                .map(question -> imageQuestionService.getHintForImageQuestion(question.getImageUrl(), playerQuestion))
                .orElse("No se encontró la imagen asociada a la pregunta");
    }


}
