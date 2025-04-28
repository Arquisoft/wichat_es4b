package com.uniovi.test.cobertura;

import com.uniovi.controllers.GameController;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.GameSession;
import com.uniovi.entities.Player;
import com.uniovi.entities.Question;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.PlayerService;
import com.uniovi.services.impl.GameSessionServiceImpl;
import com.uniovi.services.impl.MultiplayerSessionServiceImpl;
import com.uniovi.services.impl.QuestionServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {

    @Mock
    private QuestionServiceImpl questionService;

    @Mock
    private GameSessionServiceImpl gameSessionService;

    @Mock
    private PlayerService playerService;

    @Mock
    private MultiplayerSessionServiceImpl multiplayerSessionService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private Principal principal;

    @InjectMocks
    private GameController gameController;

    private GameSession gameSession;
    private Question question;
    private Player player;

    @BeforeEach
    void setup() {
        player = new Player();
        player.setId(1L);
        player.setUsername("testUser");

        question = new Question();
        question.setId(1L);
        question.setStatement("Test question?");

        gameSession = new GameSession();
        gameSession.setPlayer(player);
        gameSession.setCorrectQuestions(0);

        List<Question> questions = new ArrayList<>();
        questions.add(question);
        gameSession.setQuestionsToAnswer(questions);
        gameSession.setCurrentQuestion(question);
    }


    @Test
    void getCheckResult_NoSession_RedirectsToGame() {
        // Arrange
        when(session.getAttribute("gameSession")).thenReturn(null);

        // Act
        String result = gameController.getCheckResult(1L, 2L, model, session);

        // Assert
        assertEquals("redirect:/game/pregunta", result);
    }


    @Test
    void getPoints_ValidSession_ReturnsCorrectPoints() {
        // Arrange
        gameSession.setCorrectQuestions(5);
        when(session.getAttribute("gameSession")).thenReturn(gameSession);

        // Act
        String result = gameController.getPoints(session);

        // Assert
        assertEquals("5", result);
    }

    @Test
    void getPoints_NoSession_ReturnsZero() {
        // Arrange
        when(session.getAttribute("gameSession")).thenReturn(null);

        // Act
        String result = gameController.getPoints(session);

        // Assert
        assertEquals("0", result);
    }


    @Test
    void getCurrentQuestion_NoSession_ReturnsZero() {
        // Arrange
        when(session.getAttribute("gameSession")).thenReturn(null);

        // Act
        String result = gameController.getCurrentQuestion(session);

        // Assert
        assertEquals("0", result);
    }

}