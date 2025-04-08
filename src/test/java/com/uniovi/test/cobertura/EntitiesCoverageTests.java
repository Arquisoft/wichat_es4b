package com.uniovi.test.cobertura;

import com.uniovi.entities.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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

        Associations.PlayerRole.addRole(player, role);
        assertTrue(player.getRoles().contains(role));
        assertTrue(role.getPlayers().contains(player));

        Associations.PlayerRole.removeRole(player, role);
        assertFalse(player.getRoles().contains(role));
        assertFalse(role.getPlayers().contains(player));
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
    @Order(557)
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
    }

    @Test
    @Order(558)
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
    }

    @Test
    @Order(559)
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
    @Order(560)
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
    @Order(561)
    public void testLanguage() {
        // Verificar la conversión de códigos de idioma
        assertEquals(Language.EN, Language.fromCode("en"));
        assertEquals(Language.ES, Language.fromCode("es"));
        assertThrows(IllegalArgumentException.class, () -> Language.fromCode("invalid"));

        // Verificar la representación en cadena
        assertEquals("en", Language.EN.toString());
        assertEquals("es", Language.ES.toString());
    }
}
