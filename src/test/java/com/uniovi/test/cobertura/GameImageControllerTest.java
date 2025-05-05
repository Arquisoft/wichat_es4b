package com.uniovi.test.cobertura;

import com.uniovi.controllers.GameImageController;
import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.GameSessionImage;
import com.uniovi.entities.Player;
import com.uniovi.entities.QuestionImage;
import com.uniovi.services.impl.GameSessionImageServiceImpl;
import com.uniovi.services.impl.MultiplayerSessionImageServiceImpl;
import com.uniovi.services.impl.PlayerServiceImpl;
import com.uniovi.services.impl.QuestionImageServiceImpl;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameImageControllerTest {

    @Mock
    private QuestionImageServiceImpl questionService;

    @Mock
    private GameSessionImageServiceImpl gameSessionService;

    @Mock
    private PlayerServiceImpl playerService;

    @Mock
    private MultiplayerSessionImageServiceImpl multiplayerSessionService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private Principal principal;

    @InjectMocks
    private GameImageController gameImageController;

    private GameSessionImage gameSession;
    private QuestionImage question;
    private Player player;

    @BeforeEach
    void setup() {
        player = new Player();
        player.setId(1L);
        player.setUsername("testUser");

        question = new QuestionImage();
        question.setId(1L);
        question.setStatement("Test question?");
        question.setImageUrl("test-image.jpg");

        gameSession = new GameSessionImage();
        gameSession.setPlayer(player);
        gameSession.setScore(0);

        List<QuestionImage> questions = new ArrayList<>();
        questions.add(question);
        gameSession.setQuestionsToAnswer(questions);
        gameSession.setCurrentQuestion(question);
    }





    @Test
    void getCheckResult_NoSession_RedirectsToGame() {
        // Arrange
        when(session.getAttribute("gameSessionImage")).thenReturn(null);

        // Act
        String result = gameImageController.getCheckResult(1L, 2L, model, session);

        // Assert
        assertEquals("redirect:/game/image/game", result);
    }



    @Test
    void updateGame_NoNextQuestionMultiplayer_ThrowsException() {
        // Arrange
        gameSession.setCurrentQuestion(null);
        gameSession.setMultiplayer(true);
        when(session.getAttribute("gameSessionImage")).thenReturn(gameSession);

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> gameImageController.updateGame(model, session));
    }


    @Test
    void getPoints_ValidSession_ReturnsCorrectPoints() {
        // Arrange
        gameSession.setScore(5);
        when(session.getAttribute("gameSessionImage")).thenReturn(gameSession);

        // Act
        String result = gameImageController.getPoints(session);

        // Assert
        assertEquals("5", result);
    }

    @Test
    void getPoints_NoSession_ReturnsZero() {
        // Arrange
        when(session.getAttribute("gameSessionImage")).thenReturn(null);

        // Act
        String result = gameImageController.getPoints(session);

        // Assert
        assertEquals("0", result);
    }



    @Test
    void getCurrentQuestion_NoSession_ReturnsZero() {
        // Arrange
        when(session.getAttribute("gameSessionImage")).thenReturn(null);

        // Act
        String result = gameImageController.getCurrentQuestion(session);

        // Assert
        assertEquals("0", result);
    }


    @Test
    void getImageQuestionHint_InvalidQuestion_ReturnsErrorMessage() {
        // Arrange
        when(questionService.getQuestion(999L)).thenReturn(Optional.empty());

        // Act
        String result = gameImageController.getImageQuestionHint(999L, "gpt4");

        // Assert
        assertEquals("No se encontró ninguna pista para esta pregunta.", result);
    }
}