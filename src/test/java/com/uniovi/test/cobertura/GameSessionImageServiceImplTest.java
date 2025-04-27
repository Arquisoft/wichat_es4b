package com.uniovi.test.cobertura;

import com.uniovi.entities.GameSessionImage;
import com.uniovi.entities.Player;
import com.uniovi.entities.QuestionImage;
import com.uniovi.repositories.GameSessionImageRepository;
import com.uniovi.services.impl.GameSessionImageServiceImpl;
import com.uniovi.services.impl.MultiplayerSessionImageServiceImpl;
import com.uniovi.services.impl.QuestionImageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class GameSessionImageServiceImplTest {

    @Mock
    private GameSessionImageRepository gameSessionRepository;

    @Mock
    private QuestionImageServiceImpl questionService;

    @Mock
    private MultiplayerSessionImageServiceImpl multiplayerSessionService;

    @InjectMocks
    private GameSessionImageServiceImpl gameSessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getGameSessions_ReturnsAllSessions() {
        // Arrange
        List<GameSessionImage> sessions = List.of(new GameSessionImage(), new GameSessionImage());
        when(gameSessionRepository.findAll()).thenReturn(sessions);

        // Act
        List<GameSessionImage> result = gameSessionService.getGameSessions();

        // Assert
        assertEquals(2, result.size());
        verify(gameSessionRepository, times(1)).findAll();
    }

    @Test
    void getGameSessionsByPlayer_ReturnsPlayerSessions() {
        // Arrange
        Player player = new Player();
        List<GameSessionImage> sessions = List.of(new GameSessionImage());
        when(gameSessionRepository.findAllByPlayer(player)).thenReturn(sessions);

        // Act
        List<GameSessionImage> result = gameSessionService.getGameSessionsByPlayer(player);

        // Assert
        assertEquals(1, result.size());
        verify(gameSessionRepository, times(1)).findAllByPlayer(player);
    }

    @Test
    void getGlobalRanking_ReturnsPageOfScores() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<Object[]> page = new PageImpl<>(Collections.emptyList());
        when(gameSessionRepository.findScoresByPlayer(pageable)).thenReturn(page);

        // Act
        Page<Object[]> result = gameSessionService.getGlobalRanking(pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        verify(gameSessionRepository, times(1)).findScoresByPlayer(pageable);
    }

    @Test
    void getPlayerRanking_ReturnsPageOfSessions() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Player player = new Player();
        Page<GameSessionImage> page = new PageImpl<>(Collections.emptyList());
        when(gameSessionRepository.findAllByPlayerOrderByScoreDesc(pageable, player)).thenReturn(page);

        // Act
        Page<GameSessionImage> result = gameSessionService.getPlayerRanking(pageable, player);

        // Assert
        assertEquals(0, result.getTotalElements());
        verify(gameSessionRepository, times(1)).findAllByPlayerOrderByScoreDesc(pageable, player);
    }

    @Test
    void startNewGame_ReturnsNewGameSession() {
        // Arrange
        Player player = new Player();
        List<QuestionImage> questions = Arrays.asList(new QuestionImage(), new QuestionImage(), new QuestionImage(), new QuestionImage());
        when(questionService.getRandomQuestions(GameSessionImageServiceImpl.NORMAL_GAME_QUESTION_NUM)).thenReturn(questions);

        // Act
        GameSessionImage session = gameSessionService.startNewGame(player);

        // Assert
        assertNotNull(session);
        assertEquals(player, session.getPlayer());
        verify(questionService, times(1)).getRandomQuestions(GameSessionImageServiceImpl.NORMAL_GAME_QUESTION_NUM);
    }

    @Test
    void startNewMultiplayerGame_WithValidCode_ReturnsSession() {
        // Arrange
        Player player = new Player();
        List<QuestionImage> questions = Arrays.asList(new QuestionImage(), new QuestionImage());
        when(multiplayerSessionService.getQuestions("1234")).thenReturn(questions);

        // Act
        GameSessionImage session = gameSessionService.startNewMultiplayerGame(player, 1234);

        // Assert
        assertNotNull(session);
        assertTrue(session.isMultiplayer());
        assertEquals(player, session.getPlayer());
        verify(multiplayerSessionService, times(1)).getQuestions("1234");
    }

    @Test
    void startNewMultiplayerGame_WithInvalidCode_ReturnsNull() {
        // Arrange
        when(multiplayerSessionService.getQuestions("9999")).thenReturn(null);

        // Act
        GameSessionImage session = gameSessionService.startNewMultiplayerGame(new Player(), 9999);

        // Assert
        assertNull(session);
        verify(multiplayerSessionService, times(1)).getQuestions("9999");
    }

    @Test
    void endGame_SavesFinishedGame() {
        // Arrange
        Player player = new Player();
        GameSessionImage session = new GameSessionImage();
        session.setPlayer(player);

        // Act
        gameSessionService.endGame(session);

        // Assert
        assertNotNull(session.getFinishTime());
        verify(gameSessionRepository, times(1)).save(session);
    }
}