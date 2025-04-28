package com.uniovi.test.cobertura;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.entities.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unitTest coverage for Entities")
@TestMethodOrder(OrderAnnotation.class)
public class EntitiesCoverageTests {

	// Test de cobertura de las Entities. Reservado de la 550 a la 559

	@Test
	@Order(550)
	public void testAnswer() {
		// Answer
		Answer answer = new Answer("Correct answer", true);
		assertNotNull(answer);
		assertEquals("Correct answer", answer.getText());
		assertTrue(answer.isCorrect());

		// Test toString method
		assertEquals("Correct answer", answer.toString());
	}

	@Test
	@Order(551)
	public void testAnswerImage() {
		AnswerImage answerImage = new AnswerImage("Image answer", true);
		assertNotNull(answerImage);
		assertEquals("Image answer", answerImage.getText());
		assertTrue(answerImage.isCorrect());

		// Test toString method
		assertEquals("Image answer", answerImage.toString());
	}

	@Test
	@Order(552)
	public void testPlayerRoleAssociation() {
		Player player = new Player();
		Role role = new Role("ROLE_PLAYER");

		assertEquals(role.toString(), role.getName());

		Associations.PlayerRole.addRole(player, role);
		assertTrue(player.getRoles().contains(role));
		assertTrue(role.getPlayers().contains(player));

		Associations.PlayerRole.removeRole(player, role);
		assertFalse(player.getRoles().contains(role));
		assertFalse(role.getPlayers().contains(player));

		assertFalse(player.toString().isEmpty());
		assertFalse(player.toString().isBlank());
	}

	@Test
	@Order(553)
	public void testPlayerApiKeyAssociation() {
		Player player = new Player();
		ApiKey apiKey = new ApiKey();

		Associations.PlayerApiKey.addApiKey(player, apiKey);
		assertEquals(apiKey, player.getApiKey());
		assertEquals(player, apiKey.getPlayer());

		Associations.PlayerApiKey.removeApiKey(player, apiKey);
		assertNull(player.getApiKey());
		assertNull(apiKey.getPlayer());
	}

	@Test
	@Order(554)
	public void testApiKeyAccessLogAssociation() {
		ApiKey apiKey = new ApiKey();
		RestApiAccessLog accessLog = new RestApiAccessLog();

		Associations.ApiKeyAccessLog.addAccessLog(apiKey, accessLog);
		assertTrue(apiKey.getAccessLogs().contains(accessLog));
		assertEquals(apiKey, accessLog.getApiKey());

		Associations.ApiKeyAccessLog.removeAccessLog(apiKey, accessLog);
		assertFalse(apiKey.getAccessLogs().contains(accessLog));
		assertNull(accessLog.getApiKey());
	}

	@Test
	@Order(555)
	public void testPlayerGameSessionAssociation() {
		Player player = new Player();
		GameSession gameSession = new GameSession();

		Associations.PlayerGameSession.addGameSession(player, gameSession);
		assertTrue(player.getGameSessions().contains(gameSession));
		assertEquals(player, gameSession.getPlayer());

		Associations.PlayerGameSession.removeGameSession(player, gameSession);
		assertFalse(player.getGameSessions().contains(gameSession));
		assertNull(gameSession.getPlayer());
	}

	@Test
	@Order(556)
	public void testPlayerGameSessionImageAssociation() {
		Player player = new Player();
		GameSessionImage gameSession = new GameSessionImage();

		Associations.PlayerGameSessionImage.addGameSession(player, gameSession);
		assertTrue(player.getGameSessionsImage().contains(gameSession));
		assertEquals(player, gameSession.getPlayer());

		Associations.PlayerGameSessionImage.removeGameSession(player, gameSession);
		assertFalse(player.getGameSessionsImage().contains(gameSession));
		assertNull(gameSession.getPlayer());
	}

	@Test
	@Order(557)
	public void testQuestionCategoryAssociation() {
		Question question = new Question();
		Category category = new Category("History");

		Associations.QuestionsCategory.addCategory(question, category);
		assertEquals(category, question.getCategory());
		assertTrue(category.getQuestions().contains(question));

		Associations.QuestionsCategory.removeCategory(question, category);
		assertNull(question.getCategory());
		assertFalse(category.getQuestions().contains(question));
	}

	@Test
	@Order(558)
	public void testQuestionImageCategoryAssociation() {
		QuestionImage questionImage = new QuestionImage();
		Category category = new Category("Animals");

		Associations.QuestionsImageCategory.addCategory(questionImage, category);
		assertEquals(category, questionImage.getCategory());
		assertTrue(category.getQuestionsImageGame().contains(questionImage));

		Associations.QuestionsImageCategory.removeCategory(questionImage, category);
		assertNull(questionImage.getCategory());
		assertFalse(category.getQuestionsImageGame().contains(questionImage));
	}

	@Test
	@Order(559)
	public void testQuestionImageAnswerAssociation() {
		QuestionImage questionImage = new QuestionImage();
		List<AnswerImage> answerImage = new ArrayList<>();
		answerImage.add(new AnswerImage("Italia", true));
		answerImage.add(new AnswerImage("Madrid", false));
		answerImage.add(new AnswerImage("Guatemala", false));
		answerImage.add(new AnswerImage("Paris", false));

		// Añadir las respuestas a la pregunta
		Associations.QuestionImageAnswers.addAnswer(questionImage, answerImage);

		// Verificar que cada respuesta se ha añadido correctamente
		for (AnswerImage ai : answerImage) {
			assertTrue(questionImage.getOptions().contains(ai));
			assertEquals(questionImage, ai.getQuestion());
		}

		// Eliminar las respuestas
		Associations.QuestionImageAnswers.removeAnswer(questionImage, answerImage);

		// Verificar que cada respuesta se ha eliminado correctamente
		for (AnswerImage ai : answerImage) {
			assertFalse(questionImage.getOptions().contains(ai));
			assertNull(ai.getQuestion());
		}
	}


	@Test
	@Order(560)
	public void testApiKey() {
		// Crear un ApiKey
		ApiKey apiKey = new ApiKey();
		assertNotNull(apiKey);
		assertNotNull(apiKey.getKeyToken()); // Verifica que el token no sea null

		// Asociar un ApiKey con un Player
		Player player = new Player();
		Associations.PlayerApiKey.addApiKey(player, apiKey);
		assertEquals(player, apiKey.getPlayer());

		// Verificar la relación inversa
		assertSame(player.getApiKey(), apiKey);

		// Añadir logs de acceso
		RestApiAccessLog accessLog = new RestApiAccessLog();
		apiKey.getAccessLogs().add(accessLog);
		assertTrue(apiKey.getAccessLogs().contains(accessLog));
	}

	@Test
	@Order(561)
	public void testCategory() {
		// Crear una categoría
		Category category = new Category("Science", "Questions about Science");
		assertNotNull(category);
		assertEquals("Science", category.getName());
		assertEquals("Questions about Science", category.getDescription());

		// Verificar la relación con las preguntas
		Question question = new Question();
		category.getQuestions().add(question);
		assertTrue(category.getQuestions().contains(question));

		assertEquals(category.toString(), category.getName());
	}

	@Test
	@Order(562)
	public void testGameSession() {
		// Crear un GameSession
		Player player = new Player();
		List<Question> questions = new ArrayList<>();
		GameSession gameSession = new GameSession(player, questions);

		assertNotNull(gameSession);
		assertEquals(player, gameSession.getPlayer());
		assertEquals(questions, gameSession.getQuestionsToAnswer());

		// Comprobar las propiedades calculadas
		assertEquals("00:00:00", gameSession.getDuration());
		assertFalse(gameSession.isFinished());

		gameSession.setCreatedAt(null);
		gameSession.setFinishTime(null);
		assertEquals("00:00:00", gameSession.getDuration());
		gameSession.setCreatedAt(LocalDateTime.now());
		assertEquals("00:00:00", gameSession.getDuration());
	}

	@Test
	@Order(563)
	public void testGameSessionImage() {
		// Crear un GameSessionImage
		Player player = new Player();
		List<QuestionImage> questionsImage = new ArrayList<>();
		GameSessionImage gameSessionImage = new GameSessionImage(player, questionsImage);

		assertNotNull(gameSessionImage);
		assertEquals(player, gameSessionImage.getPlayer());
		assertEquals(questionsImage, gameSessionImage.getQuestionsToAnswer());
	}

	@Test
	@Order(564)
	public void testMultiplayerSession() {
		// Crear una sesión multijugador
		Player player1 = new Player();
		Player player2 = new Player();
		MultiplayerSession multiplayerSession = new MultiplayerSession("ABC123", player1);

		assertNotNull(multiplayerSession);
		assertEquals("ABC123", multiplayerSession.getMultiplayerCode());
		assertTrue(multiplayerSession.getPlayerScores().containsKey(player1));

		// Añadir un nuevo jugador
		multiplayerSession.addPlayer(player2);
		assertTrue(multiplayerSession.getPlayerScores().containsKey(player2));
	}

	@Test
	@Order(565)
	public void testLanguage() {
		// Verificar la conversión de códigos de idioma
		assertEquals(Language.EN, Language.fromCode("en"));
		assertEquals(Language.ES, Language.fromCode("es"));
		assertThrows(IllegalArgumentException.class, () -> Language.fromCode("invalid"));

		// Verificar la representación en cadena
		assertEquals("en", Language.EN.toString());
		assertEquals("es", Language.ES.toString());
	}

	@Test
	@Order(566)
	public void testQuestion() {
		// Crear categoría y lenguaje
		Category category = new Category("Geography");
		Language language = Language.EN;

		// Crear respuestas para Question
		Answer a1 = new Answer("Paris", true);
		Answer a2 = new Answer("London", false);
		Answer a3 = new Answer("Berlin", false);
		List<Answer> answers = List.of(a1, a2, a3);

		// Crear una pregunta con texto
		Question question = new Question("What is the capital of France?", answers, a1,
										 category, language);

		// Verificaciones básicas
		assertEquals("What is the capital of France?", question.getStatement());
		assertEquals(3, question.getOptions().size());
		assertTrue(question.isCorrectAnswer(a1));
		assertFalse(question.isCorrectAnswer(a2));
		assertNotNull(question.getOption(0));
		assertNotNull(question.getOptions("Paris"));
		assertTrue(question.getOptions().contains(a1));

		// Verificar toString
		String toString = question.toString();
		assertTrue(toString.contains("What is the capital of France?"));
		assertTrue(toString.contains("Answer"));

		// Verificar JSON
		JsonNode json = question.toJson();
		assertEquals("What is the capital of France?", json.get("statement").asText());
		assertEquals(3, json.get("options").size());

		// Crear respuestas para QuestionImage
		AnswerImage ai1 = new AnswerImage("cat.jpg", true);
		AnswerImage ai2 = new AnswerImage("dog.jpg", false);
		AnswerImage ai3 = new AnswerImage("rabbit.jpg", false);
		List<AnswerImage> imageAnswers = List.of(ai1, ai2, ai3);

		// Crear una pregunta con imagen
		QuestionImage questionImage = new QuestionImage("Which one is a cat?",
														imageAnswers, ai1, category,
														language, "cat_question.jpg");

		// Verificaciones básicas
		assertEquals("Which one is a cat?", questionImage.getStatement());
		assertEquals("cat_question.jpg", questionImage.getImageUrl());
		assertEquals(3, questionImage.getOptions().size());
		assertTrue(questionImage.isCorrectAnswer(ai1));
		assertNotNull(questionImage.getOptions("cat.jpg"));

		// Verificar toString
		String imgToString = questionImage.toString();
		assertTrue(imgToString.contains("Which one is a cat?"));
		assertTrue(imgToString.contains("cat_question.jpg"));

		// Verificar JSON
		JsonNode imgJson = questionImage.toJson();
		assertEquals("Which one is a cat?", imgJson.get("statement").asText());
		assertEquals("cat_question.jpg", imgJson.get("imageUrl").asText());
		assertEquals(3, imgJson.get("options").size());
	}
}
