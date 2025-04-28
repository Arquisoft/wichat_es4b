package com.uniovi.test.cobertura;

import com.uniovi.controllers.AbstractGameController;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.Player;
import com.uniovi.entities.abstracts.AbstractGameSession;
import com.uniovi.entities.abstracts.AbstractQuestion;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.PlayerService;
import com.uniovi.services.QuestionService;
import com.uniovi.services.abstracts.AbstractMultiplayerSessionService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AbstractGameControllerTest {

    private AbstractGameController<AbstractQuestion<?>, AbstractGameSession<AbstractQuestion<?>>, QuestionDto> controller;

    @Mock
    private PlayerService playerService;
    @Mock
    private AbstractMultiplayerSessionService<AbstractQuestion<?>> multiplayerSessionService;
    @Mock
    private GameSessionService<AbstractGameSession<AbstractQuestion<?>>> gameSessionService;
    @Mock
    private QuestionService<AbstractQuestion<?>, QuestionDto> questionService;

    @Mock
    private HttpSession httpSession;
    @Mock
    private Model model;
    @Mock
    private Principal principal;

    @Mock
    private AbstractGameSession<AbstractQuestion<?>> gameSession;
    @Mock
    private AbstractQuestion<?> question;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Implementamos un controlador mínimo para testear
        controller = new AbstractGameController<>(playerService, multiplayerSessionService, gameSessionService, questionService) {};
    }

    @Test
    void testGetLoggedInPlayer_Success() {
        Player expectedPlayer = new Player();
        when(principal.getName()).thenReturn("testUser");
        when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(expectedPlayer));

        Player result = controller.getLoggedInPlayer(principal);

        assertThat(result).isEqualTo(expectedPlayer);
    }

    @Test
    void testGetLoggedInPlayer_NotFound() {
        when(principal.getName()).thenReturn("unknownUser");
        when(playerService.getUserByUsername("unknownUser")).thenReturn(Optional.empty());

        Player result = controller.getLoggedInPlayer(principal);

        assertThat(result).isNull();
    }

    @Test
    void testGetSessionAttribute() {
        when(httpSession.getAttribute("gameSession")).thenReturn(gameSession);

        AbstractGameSession<?> result = controller.getSessionAttribute(httpSession);

        assertThat(result).isEqualTo(gameSession);
    }


    @Test
    void testTimeOut_WhenTimeLeft() {
        when(gameSession.getFinishTime()).thenReturn(LocalDateTime.now().plusSeconds(60));
        when(questionService.getSecondsPerQuestion()).thenReturn(60);

        boolean result = controller.timeOut(gameSession);

        assertThat(result).isFalse();
    }

    @Test
    void testTimeOut_WhenTimeout() {
        when(gameSession.getFinishTime()).thenReturn(LocalDateTime.now().minusSeconds(60));
        when(questionService.getSecondsPerQuestion()).thenReturn(60);

        boolean result = controller.timeOut(gameSession);

        assertThat(result).isTrue();
    }

    @Test
    void testCheckUpdateGameSession_AddsAnsweredQuestion_CorrectAnswer() {
        when(gameSession.isAnswered(any())).thenReturn(false);
        when(questionService.checkAnswer(anyLong(), anyLong())).thenReturn(true);
        when(gameSession.getFinishTime()).thenReturn(LocalDateTime.now().plusSeconds(60));
        when(questionService.getSecondsPerQuestion()).thenReturn(60);

        controller.checkUpdateGameSession(gameSession, 1L, 1L);

        verify(gameSession, times(1)).addAnsweredQuestion(any());
        verify(gameSession, times(1)).addQuestion(eq(true), anyInt());
    }


    @Test
    void testCheckUpdateGameSession_NoTimeout() {
        when(gameSession.getFinishTime()).thenReturn(LocalDateTime.now().plusSeconds(60));
        when(questionService.getSecondsPerQuestion()).thenReturn(60);

        boolean result = controller.checkUpdateGameSession(gameSession, httpSession);

        assertThat(result).isFalse();
    }
}
