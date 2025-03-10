package com.uniovi.controllers;

import com.uniovi.entities.GameSessionImage;
import com.uniovi.entities.Player;
import com.uniovi.entities.PlayerImage;
import com.uniovi.entities.QuestionImage;
import com.uniovi.services.PlayerService;
import com.uniovi.services.impl.GameSessionImageImpl;
import com.uniovi.services.impl.MultiplayerSessionImageImpl;
import com.uniovi.services.impl.PlayerServiceImageImpl;
import com.uniovi.services.impl.QuestionServiceImageImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class GameImageController {
    private static final String GAMESESSION_STR = "gameSessionImage";
    private final QuestionServiceImageImpl questionService;
    private final GameSessionImageImpl gameSessionImpl;
    private final PlayerServiceImageImpl playerService;

    private final MultiplayerSessionImageImpl multiplayerSessionImageImpl;

    public GameImageController(QuestionServiceImageImpl questionImageService, GameSessionImageImpl gameSessionImageImpl,
                               PlayerServiceImageImpl playerService, MultiplayerSessionImageImpl multiplayerSessionImageImpl) {
        this.questionService = questionImageService;
        this.gameSessionImpl = gameSessionImageImpl;
        this.playerService = playerService;
        this.multiplayerSessionImageImpl = multiplayerSessionImageImpl;
    }

    /**
     * This method is used to get the game view and to start the game
     * @param model The model to be used
     * @return The view to be shown
     */
    @GetMapping("/game/image")
    public String getGame(HttpSession session, Model model, Principal principal) {
        GameSessionImage gameSessionImage = (GameSessionImage) session.getAttribute(GAMESESSION_STR);
        if (gameSessionImage != null && !gameSessionImage.isFinished() && !gameSessionImage.isMultiplayer()) {
            if (checkUpdateGameSession(gameSessionImage, session)) {
                return "game/image/fragments/gameFinished";
            }
        } else {
            gameSessionImage = gameSessionImpl.startNewGame(getLoggedInPlayer(principal));
            session.setAttribute(GAMESESSION_STR, gameSessionImage);
            playerService.deleteMultiplayerCode(gameSessionImage.getPlayer().getId());
        }

        model.addAttribute("questionImage", gameSessionImage.getCurrentQuestionImage());
        model.addAttribute("questionDurationImage", getRemainingTime(gameSessionImage));
        return "game/image";
    }

    @GetMapping("/game/image/multiplayer")
    public String getImageMultiplayerGame() {
        return "redirect:/image/multiplayerGame/createGame";
    }



    @GetMapping("/image/multiplayerGame/{code}")
    public String joinMultiplayerGame(@PathVariable String code, HttpSession session, Principal principal, Model model) {
        if (!multiplayerSessionImageImpl.existsCode(code)) {
            model.addAttribute("errorKeyImage", "multi.code.invalid");
            return "game/image/multiplayerGameImage";
        }

        Optional<PlayerImage> player = playerService.getUserByUsername(principal.getName());
        PlayerImage p = player.orElse(null);
        if (playerService.changeMultiplayerCode(p.getId(),code)) {
            multiplayerSessionImageImpl.addToLobby(code,p.getId());
            model.addAttribute("multiplayerGameCodeImage",code);
            session.setAttribute("multiplayerCodeImage",code);
            return "redirect:/game/image/lobby";
        } else {
            return "redirect:/image/multiplayerGame";
        }
    }

    @GetMapping("/image/multiplayerGame/createGame")
    public String createMultiplayerGame(HttpSession session, Principal principal, Model model) {
        Optional<PlayerImage> player = playerService.getUserByUsername(principal.getName());
        PlayerImage p = player.orElse(null);
        String code="" + playerService.createMultiplayerGame(p.getId());//playerService.createMultiplayerGameImage(p.getId())
        multiplayerSessionImageImpl.multiCreate(code,p.getId());
        session.setAttribute("multiplayerCodeImage",code);
        return "redirect:/game/lobby";
    }

    @GetMapping("/image/startMultiplayerGame")
    public String startMultiplayerGame(HttpSession session, Model model, Principal principal) {
        GameSessionImage gameSessionImage = (GameSessionImage) session.getAttribute("gameSessionImage");

        if (gameSessionImage != null) {
            if (! gameSessionImage.isMultiplayer()) {
                session.removeAttribute("gameSessionImage");
                return "redirect:/image/startMultiplayerGame";
            }

            if (gameSessionImage.isFinished()) {
                model.addAttribute("codeImage", session.getAttribute("multiplayerCodeImage"));
                return "game/image/multiplayerFinished";
            }

            if (checkUpdateGameSession(gameSessionImage, session)) {
                return "game/image/fragments/gameFinished";
            }
        } else {
            Optional<PlayerImage> player = playerService.getUserByUsername(principal.getName());
            if (!player.isPresent()) {
                return "redirect:/";
            }
            gameSessionImage = gameSessionImpl.startNewMultiplayerGame(getLoggedInPlayer(principal),
                    player.get().getMultiplayerCode());
            if (gameSessionImage == null)
                return "redirect:/image/multiplayerGame";
            session.setAttribute("gameSessionImage", gameSessionImage);
        }

        model.addAttribute("questionImage", gameSessionImage.getCurrentQuestionImage());
        model.addAttribute("questionDurationImage", getRemainingTime(gameSessionImage));
        return "game/image";
    }

    @GetMapping("/image/multiplayerGame/endGame/{code}")
    public String endMultiplayerGame(Model model,@PathVariable String code) {
        model.addAttribute("codeImage",code);
        return "ranking/image/multiplayerRanking";
    }

    @GetMapping("/image/endGameList/{code}")
    @ResponseBody
    public Map<String, String> endMultiplayerGameTable(@PathVariable String code) {
        Map<Player, Integer> playerScores = multiplayerSessionImageImpl.getPlayersWithScores(Integer.parseInt(code));
        Map<String, String> playersNameWithScore=new HashMap<>();
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

    @GetMapping("/game/image/lobby/{code}")
    @ResponseBody
    public List<String> updatePlayerList(@PathVariable String code) {
        Map<Player,Integer> players= multiplayerSessionImageImpl.getPlayersWithScores(Integer.parseInt(code));
        List<String> playerNames = new ArrayList<>();
        for (Map.Entry<Player, Integer> player : players.entrySet()) {
            playerNames.add(player.getKey().getUsername());
        }
        Collections.sort(playerNames);
        return playerNames;
    }

    @GetMapping("/game/image/lobby")
    public String createLobby( HttpSession session, Model model) {
        int code = Integer.parseInt((String)session.getAttribute("multiplayerCodeImage"));
        List<PlayerImage> players = playerService.getUsersByMultiplayerCode(code);
        model.addAttribute("playersImage",players);
        model.addAttribute("codeImage",session.getAttribute("multiplayerCodeImage"));
        return "game/image/lobby";
    }

    /**
     * This method is used to check the answer for a specific question
     * @param idQuestion The id of the question.
     * @param idAnswer The id of the answer. If the id is -1, it means that the user has not selected any answer and the
     *                 time has run out.
     * @param model The model to be used.
     * @return The view to be shown, if the answer is correct, the success view is shown, otherwise the failure view is
     * shown or the timeOutFailure view is shown.
     */
    @GetMapping("/game/image/{idQuestionImage}/{idAnswerImage}")
    public String getCheckResult(@PathVariable Long idQuestion, @PathVariable Long idAnswer, Model model, HttpSession session, Principal principal) {
        GameSessionImage gameSessionImage = (GameSessionImage) session.getAttribute(GAMESESSION_STR);
        if (gameSessionImage == null) {
            return "redirect:/game/image";
        }

        if (!gameSessionImage.hasQuestionImageId(idQuestion)) {
            model.addAttribute("scoreImage", gameSessionImage.getScore());
            session.removeAttribute(GAMESESSION_STR);
            return "redirect:/game/image"; // if someone wants to exploit the game, just redirect to the game page
        }

        if(idAnswer == -1
            || getRemainingTime(gameSessionImage) <= 0) {
            gameSessionImage.addAnsweredQuestion(gameSessionImage.getCurrentQuestionImage());
            gameSessionImage.addQuestion(false, 0);
        }
        else if(questionService.checkAnswer(idQuestion, idAnswer)) {
            if (!gameSessionImage.isAnswered(gameSessionImage.getCurrentQuestionImage())) {
                gameSessionImage.addQuestion(true, getRemainingTime(gameSessionImage));
                gameSessionImage.addAnsweredQuestion(gameSessionImage.getCurrentQuestionImage());
            }
        } else {
            gameSessionImage.addAnsweredQuestion(gameSessionImage.getCurrentQuestionImage());
            gameSessionImage.addQuestion(false, 0);
        }

        session.setAttribute("hasJustAnsweredImage", true);
        gameSessionImage.getNextQuestion();
        return updateGame(model, session, principal);
    }

    @GetMapping("/game/image/update")
    public String updateGame(Model model, HttpSession session, Principal principal) {
        GameSessionImage gameSessionImage = (GameSessionImage) session.getAttribute(GAMESESSION_STR);
        QuestionImage nextQuestionImage = gameSessionImage.getCurrentQuestionImage();
        if (nextQuestionImage == null && gameSessionImage.isMultiplayer()) {
            int code = Integer.parseInt((String) session.getAttribute("multiplayerCodeImage"));
            List<PlayerImage> players = playerService.getUsersByMultiplayerCode(code);

            if (!gameSessionImage.isFinished()) {
                gameSessionImpl.endGame(gameSessionImage);

                model.addAttribute("playersImage", players);
                model.addAttribute("codeImage", session.getAttribute("multiplayerCodeImage"));
                gameSessionImage.setFinished(true);

                Optional<PlayerImage> player = playerService.getUserByUsername(principal.getName());
                PlayerImage p = player.orElse(null);
                playerService.setScoreMultiplayerCode(p.getId(),"" + gameSessionImage.getScore());
                multiplayerSessionImageImpl.changeScore(p.getMultiplayerCode()+"",p.getId(),gameSessionImage.getScore());
            } else {
                model.addAttribute("playersImage", players);

            }

            model.addAttribute("codeImage", session.getAttribute("multiplayerCodeImage"));
            return "image/ranking/multiplayerRanking";
        }

        if (nextQuestionImage == null) {
            if (!gameSessionImage.isFinished()) {
                gameSessionImpl.endGame(gameSessionImage);
                gameSessionImage.setFinished(true);
            } else {
                session.removeAttribute(GAMESESSION_STR);
                model.addAttribute("scoreImage", gameSessionImage.getScore());
            }
            return "game/image/fragments/gameFinished";
        }

        if (session.getAttribute("hasJustAnsweredImage") != null) {
            if ((boolean) session.getAttribute("hasJustAnsweredImage"))
                gameSessionImage.setFinishTime(LocalDateTime.now());
            session.removeAttribute("hasJustAnsweredImage");
        }
        model.addAttribute("questionImage", gameSessionImage.getCurrentQuestionImage());
        model.addAttribute("questionDurationImage", getRemainingTime(gameSessionImage));
        return "game/image/fragments/gameFrame";
    }

    @GetMapping("/game/pointsImage")
    @ResponseBody
    public String getPoints(HttpSession session) {
        GameSessionImage gameSessionImage = (GameSessionImage) session.getAttribute(GAMESESSION_STR);
        if (gameSessionImage != null)
            return String.valueOf(gameSessionImage.getScore());
        else
            return "0";
    }

    @GetMapping("/game/currentQuestionImage")
    @ResponseBody
    public String getCurrentQuestion(HttpSession session) {
        GameSessionImage gameSessionImage = (GameSessionImage) session.getAttribute(GAMESESSION_STR);
        if (gameSessionImage != null)
            return String.valueOf(Math.min(gameSessionImage.getAnsweredQuestions().size()+1, GameSessionImageImpl.NORMAL_GAME_QUESTION_NUM));
        else
            return "0";
    }

    private PlayerImage getLoggedInPlayer(Principal principal) {
        Optional<PlayerImage> player = playerService.getUserByUsername(principal.getName());
        return player.orElse(null);
    }

    /**
     * This method is used to check if the game session has to be updated
     * @param gameSessionImage The game session to be checked
     * @param session The session to be used
     * @return True if the game session has been ended, false otherwise
     */
    private boolean checkUpdateGameSession(GameSessionImage gameSessionImage, HttpSession session) {
        // if time since last question started is greater than the time per question, add a new question (or check for game finish)
        if (getRemainingTime(gameSessionImage) <= 0
                && gameSessionImage.getQuestionsToAnswer().isEmpty()
                && gameSessionImage.getCurrentQuestionImage() != null) {
            gameSessionImage.addQuestion(false, 0);
            gameSessionImage.addAnsweredQuestion(gameSessionImage.getCurrentQuestionImage());
            if (gameSessionImage.getQuestionsToAnswer().isEmpty()) {
                gameSessionImpl.endGame(gameSessionImage);
                session.removeAttribute(GAMESESSION_STR);
                return true;
            }
        }

        return false;
    }

    private int getRemainingTime(GameSessionImage gameSessionImage) {
        return (int) Duration.between(LocalDateTime.now(),
                gameSessionImage.getFinishTime().plusSeconds(QuestionServiceImageImpl.SECONDS_PER_QUESTION)).toSeconds();
    }
}
