package com.uniovi.controllers;

import com.uniovi.entities.GameSession;
import com.uniovi.entities.ImageQuestion;
import com.uniovi.entities.Player;
import com.uniovi.entities.Question;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.ImageQuestionService;
import com.uniovi.services.MultiplayerSessionService;
import com.uniovi.services.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final MultiplayerSessionService multiplayerSessionService;

    public ImageQuestionController(ImageQuestionService imageQuestionService, GameSessionService gameSessionService,
                                   PlayerService playerService, MultiplayerSessionService multiplayerSessionService) {
        this.imageQuestionService = imageQuestionService;
        this.gameSessionService = gameSessionService;
        this.playerService = playerService;
        this.multiplayerSessionService = multiplayerSessionService;
    }

    /**
     * This method is used to get the game view and to start the game with image questions
     * @param model The model to be used
     * @return The view to be shown
     */
    @GetMapping("/game/image")
    public String getImageGame(HttpSession session, Model model, Principal principal) {
        GameSession gameSession = (GameSession) session.getAttribute(GAMESESSION_STR);
        if (gameSession != null && !gameSession.isFinished()) {
            if (checkUpdateGameSession(gameSession, session)) {
                return "game/fragments/gameFinished";
            }
        } else {
            gameSession = gameSessionService.startNewImageGame(getLoggedInPlayer(principal));
            session.setAttribute(GAMESESSION_STR, gameSession);
            playerService.deleteMultiplayerCode(gameSession.getPlayer().getId());
        }

        model.addAttribute("question", gameSession.getCurrentImageQuestion());
        model.addAttribute("questionDuration", getRemainingTime(gameSession));
        return "game/image";
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

    @GetMapping("/multiplayerGame/image/createGame")
    public String createMultiplayerImageGame(HttpSession session, Principal principal, Model model) {
        Optional<Player> player = playerService.getUserByUsername(principal.getName());
        Player p = player.orElse(null);
        String code="" + playerService.createMultiplayerGame(p.getId());
        multiplayerSessionService.multiImageGameCreate(code,p.getId());
        session.setAttribute("multiplayerCode",code);
        return "redirect:/game/lobby";
    }

    @GetMapping("/game/image/multiplayer")
    public String getImageMultiplayerGame() {
        return "redirect:/multiplayerGame/image/createGame";
    }

    @GetMapping("/startMultiplayerImageGame")
    public String startMultiplayerImageGame(HttpSession session, Model model, Principal principal) {
        GameSession gameSession = (GameSession) session.getAttribute("gameSession");

        if (gameSession != null) {
            if (! gameSession.isMultiplayer()) {
                session.removeAttribute("gameSession");
                return "redirect:/startMultiplayerImageGame";
            }

            if (gameSession.isFinished()) {
                model.addAttribute("code", session.getAttribute("multiplayerCode"));
                return "game/multiplayerFinished";
            }

            if (checkUpdateGameSession(gameSession, session)) {
                return "game/fragments/gameFinished";
            }
        } else {
            Optional<Player> player = playerService.getUserByUsername(principal.getName());
            if (!player.isPresent()) {
                return "redirect:/";
            }
            gameSession = gameSessionService.startNewMultiplayerImageGame(getLoggedInPlayer(principal),
                    player.get().getMultiplayerCode());
            if (gameSession == null)
                return "redirect:/multiplayerGame";
            session.setAttribute("gameSession", gameSession);
        }

        model.addAttribute("question", gameSession.getCurrentQuestion());
        model.addAttribute("questionDuration", getRemainingTime(gameSession));
        return "game/basicGame";
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
    @GetMapping("/game/image/check/{idQuestion}/{idAnswer}")
    public String getCheckResult(@PathVariable Long idQuestion, @PathVariable Long idAnswer, Model model, HttpSession session, Principal principal) {
        GameSession gameSession = (GameSession) session.getAttribute(GAMESESSION_STR);
        if (gameSession == null) {
            return "redirect:/game/image";
        }

        if (!gameSession.hasQuestionId(idQuestion)) {
            model.addAttribute("score", gameSession.getScore());
            session.removeAttribute(GAMESESSION_STR);
            return "redirect:/game/image"; // if someone wants to exploit the game, just redirect to the game page
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

    @GetMapping("/game/image/update")
    public String updateGame(Model model, HttpSession session, Principal principal) {
        GameSession gameSession = (GameSession) session.getAttribute(GAMESESSION_STR);
        ImageQuestion nextQuestion = gameSession.getCurrentImageQuestion();
        if (nextQuestion == null && gameSession.isMultiplayer()) {
            int code = Integer.parseInt((String) session.getAttribute("multiplayerCode"));
            List<Player> players = playerService.getUsersByMultiplayerCode(code);

            if (!gameSession.isFinished()) {
                gameSessionService.endGame(gameSession);

                model.addAttribute("players", players);
                model.addAttribute("code", session.getAttribute("multiplayerCode"));
                gameSession.setFinished(true);

                Optional<Player> player = playerService.getUserByUsername(principal.getName());
                Player p = player.orElse(null);
                playerService.setScoreMultiplayerCode(p.getId(),"" + gameSession.getScore());
                multiplayerSessionService.changeScore(p.getMultiplayerCode()+"",p.getId(),gameSession.getScore());
            } else {
                model.addAttribute("players", players);

            }

            model.addAttribute("code", session.getAttribute("multiplayerCode"));
            return "ranking/multiplayerRanking";
        }

        if (nextQuestion == null) {
            if (!gameSession.isFinished()) {
                gameSessionService.endGame(gameSession);
                gameSession.setFinished(true);
            } else {
                session.removeAttribute(GAMESESSION_STR);
                model.addAttribute("score", gameSession.getScore());
            }
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

    @GetMapping("/game/image/points")
    @ResponseBody
    public String getPoints(HttpSession session) {
        GameSession gameSession = (GameSession) session.getAttribute(GAMESESSION_STR);
        if (gameSession != null)
            return String.valueOf(gameSession.getScore());
        else
            return "0";
    }

    @GetMapping("/game/image/currentQuestion")
    @ResponseBody
    public String getCurrentQuestion(HttpSession session) {
        GameSession gameSession = (GameSession) session.getAttribute(GAMESESSION_STR);
        if (gameSession != null)
            return String.valueOf(Math.min(gameSession.getAnsweredImageQuestions().size()+1, GameSessionService.NORMAL_GAME_QUESTION_NUM));
        else
            return "0";
    }


    @GetMapping("/game/image/hint/{id}")
    @ResponseBody
    public String getImageQuestionHint(@PathVariable Long id, @RequestParam String playerQuestion, Model model) {
        // Intenta obtener la pregunta con imagen
        // README: Hay que devolver la vista del juego de nuevo metiendo la respuesta como atributo  de Model como se viene haciendo
        // en las practicas de sdi. IN_DEVELOPMENT
        String llmHint =  imageQuestionService.getImageQuestion(id)
                .map(question -> imageQuestionService.getHintForImageQuestion(question.getImageUrl(), playerQuestion))
                .orElse("No se encontró la imagen asociada a la pregunta");
        model.addAttribute("hint", llmHint);
        return "redirect: /game/image :: hint";
    }


}
