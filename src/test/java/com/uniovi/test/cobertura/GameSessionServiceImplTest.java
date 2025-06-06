package com.uniovi.test.cobertura;

import com.uniovi.entities.GameSession;
import com.uniovi.entities.Player;
import com.uniovi.entities.Question;
import com.uniovi.repositories.GameSessionRepository;
import com.uniovi.services.GameSessionService;
import com.uniovi.services.impl.GameSessionServiceImpl;
import com.uniovi.services.impl.MultiplayerSessionServiceImpl;
import com.uniovi.services.impl.QuestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameSessionServiceImplTest {

	@Mock
	private GameSessionRepository gameSessionRepository;

	@Mock
	private QuestionServiceImpl questionService;

	@Mock
	private MultiplayerSessionServiceImpl multiplayerSessionService;

	@InjectMocks
	private GameSessionServiceImpl gameSessionService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void getGameSessions_ReturnsAllSessions() {
		// Arrange
		List<GameSession> sessions = List.of(new GameSession(), new GameSession());
		when(gameSessionRepository.findAll()).thenReturn(sessions);

		// Act
		List<GameSession> result = gameSessionService.getGameSessions();

		// Assert
		assertEquals(2, result.size());
		verify(gameSessionRepository, times(1)).findAll();
	}

	@Test
	void getGameSessionsByPlayer_ReturnsPlayerSessions() {
		// Arrange
		Player player = new Player();
		List<GameSession> sessions = List.of(new GameSession());
		when(gameSessionRepository.findAllByPlayer(player)).thenReturn(sessions);

		// Act
		List<GameSession> result = gameSessionService.getGameSessionsByPlayer(player);

		// Assert
		assertEquals(1, result.size());
		verify(gameSessionRepository, times(1)).findAllByPlayer(player);
	}

	@Test
	void getGlobalRanking_ReturnsPageOfScores() {
		// Arrange
		Pageable pageable = mock(Pageable.class);
		Page<Object[]> page = new PageImpl<>(Collections.emptyList());
		when(gameSessionRepository.findTotalScoresByPlayer(pageable)).thenReturn(page);

		// Act
		Page<Object[]> result = gameSessionService.getGlobalRanking(pageable);

		// Assert
		assertEquals(0, result.getTotalElements());
		verify(gameSessionRepository, times(1)).findTotalScoresByPlayer(pageable);
	}

	@Test
	void getPlayerRanking_ReturnsPageOfSessions() {
		// Arrange
		Pageable pageable = mock(Pageable.class);
		Player player = new Player();
		Page<GameSession> page = new PageImpl<>(Collections.emptyList());
		when(gameSessionRepository.findAllByPlayerOrderByScoreDesc(pageable,
																   player)).thenReturn(
				page);

		// Act
		Page<GameSession> result = gameSessionService.getPlayerRanking(pageable, player);

		// Assert
		assertEquals(0, result.getTotalElements());
		verify(gameSessionRepository, times(1)).findAllByPlayerOrderByScoreDesc(pageable,
																				player);
	}

	@Test
	void startNewGame_ReturnsNewGameSession() {
		// Arrange
		Player player = new Player();
		List<Question> questions = Arrays.asList(new Question(), new Question(),
												 new Question(), new Question());
		when(questionService.getRandomQuestions(
				GameSessionService.NORMAL_GAME_QUESTION_NUM)).thenReturn(questions);

		// Act
		GameSession session = gameSessionService.startNewGame(player);

		// Assert
		assertNotNull(session);
		assertEquals(player, session.getPlayer());
		verify(questionService, times(1)).getRandomQuestions(
				GameSessionService.NORMAL_GAME_QUESTION_NUM);
	}

	@Test
	void startNewMultiplayerGame_WithValidCode_ReturnsSession() {
		// Arrange
		Player player = new Player();
		List<Question> questions = Arrays.asList(new Question(), new Question());
		when(multiplayerSessionService.getQuestions("1234")).thenReturn(questions);

		// Act
		GameSession session = gameSessionService.startNewMultiplayerGame(player, 1234);

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
		GameSession session = gameSessionService.startNewMultiplayerGame(new Player(),
																		 9999);

		// Assert
		assertNull(session);
		verify(multiplayerSessionService, times(1)).getQuestions("9999");
	}

	@Test
	void endGame_SavesFinishedGame() {
		// Arrange
		Player player = new Player();
		GameSession session = new GameSession();
		session.setPlayer(player);

		// Act
		gameSessionService.endGame(session);

		// Assert
		assertNotNull(session.getFinishTime());
		verify(gameSessionRepository, times(1)).save(session);
	}
}
