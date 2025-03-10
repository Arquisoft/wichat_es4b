package com.uniovi.controllers;

import com.uniovi.entities.GameSession;
import com.uniovi.entities.GameSessionBateria;
import com.uniovi.entities.Player;
import com.uniovi.services.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class BateriaSabiosController {

    private static final String GAMESESSION_STR = "gameSession";
    @Autowired
    private QuestionService questionService;
    @Autowired //modificar para implemetar la que corresponda
    private GameSessionBateriaService gameSessionService;
    @Autowired //modificar para implemetar la que corresponda
    private PlayerService playerService;
    @Autowired
    private MultiplayerSessionService multiplayerSessionService;


    /**
     * This method is used to get the game view and to start the game
     * @param model The model to be used
     * @return The view to be shown
     */
    @GetMapping("/game/bateria")
    public String getGame(HttpSession session, Model model, Principal principal) {
        GameSessionBateria gameSession = (GameSessionBateria) session.getAttribute(GAMESESSION_STR);
        if (gameSession != null && !gameSession.isFinished() && !gameSession.isMultiplayer()) {
            if (checkUpdateGameSession(gameSession, session)) {
                return "game/fragments/gameFinished";
            }
        } else {
            gameSession = gameSessionService.startNewGame(getLoggedInPlayer(principal));
            session.setAttribute(GAMESESSION_STR, gameSession);
            playerService.deleteMultiplayerCode(gameSession.getPlayer().getId());
        }

        model.addAttribute("question", gameSession.getCurrentQuestion());
        model.addAttribute("questionDuration", getRemainingTime(gameSession));
        return "game/basicGame";
    }

    private Player getLoggedInPlayer(Principal principal) {
        Optional<Player> player = playerService.getUserByUsername(principal.getName());
        return player.orElse(null);
    }

    /**
     * This method is used to check if the game session has to be updated
     * @param gameSession The game session to be checked
     * @param session The session to be used
     * @return True if the game session has been ended, false otherwise
     */
    private boolean checkUpdateGameSession(GameSessionBateria gameSession, HttpSession session) {
        // if time since last question started is greater than the time per question, add a new question (or check for game finish)
        if (getRemainingTime(gameSession) <= 0
                && gameSession.getQuestionsToAnswer().isEmpty()
                && gameSession.getCurrentQuestion() != null) {
            gameSession.addQuestion(false);
            gameSession.addAnsweredQuestion(gameSession.getCurrentQuestion());
            if (gameSession.getQuestionsToAnswer().isEmpty()) {
                gameSessionService.endGame(gameSession);
                session.removeAttribute(GAMESESSION_STR);
                return true;
            }
        }

        return false;
    }

    private int getRemainingTime(GameSessionBateria gameSession) {
        return (int) Duration.between(LocalDateTime.now(),
                gameSession.getFinishTime().plusSeconds(QuestionService.SECONDS_PER_QUESTION)).toSeconds();
    }


}
