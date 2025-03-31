package com.uniovi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.uniovi.dto.PlayerDto;
import com.uniovi.dto.RoleDto;
import com.uniovi.entities.*;
import com.uniovi.repositories.*;
import com.uniovi.services.*;
import com.uniovi.services.impl.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
@Tag("unit")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class Wichat_UnitTests {

    @Autowired
    private PlayerServiceImpl playerService;
    @Autowired
    private QuestionServiceImpl questionService;
    @Autowired
    private QuestionImageServiceImpl questionImageService;
    @Autowired
    private QuestionGeneratorService questionGeneratorService;
    @Autowired
    private AnswerServiceImpl answerService;
    @Autowired
    private CategoryServiceImpl categoryService;
    @Autowired
    private GameSessionServiceImpl gameSessionService;
    @Autowired
    private RoleServiceImpl roleService;
    @Autowired
    private InsertSampleDataService sampleDataService;
    @Autowired
    private MultiplayerSessionServiceImpl multiplayerSessionService;
    //@Autowired
    private LlmService llmService;

    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AnswerRepository answerRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    GameSessionRepository gameSessionRepository;
    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    MultiplayerSessionRepository multiplayerSessionRepository;



    @Mock
    private RestTemplate restTemplate;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private Player createPlayer() {
        return new Player("name", "test@email.com", "password");
    }

    private Player createDiferentPlayer(String word) {
        return new Player("name" + word, word + "test@email.com", "password");
    }

    @Test
    @Order(1)
    void testPlayerService() {
        List<Player> players = playerService.getUsersByRole("ROLE_USER");
        Assertions.assertEquals(1, players.size());
    }

    @Order(3)
    void testQuestionsGenerator() throws IOException, InterruptedException {
        questionGeneratorService.generateTestQuestions();
        List<Question> questions = questionService.getAllQuestions();
        Assertions.assertFalse(questions.isEmpty());
    }

    @Test
    @Order(6)
    void testAddRole() {
        Player player = createPlayer();
        Role role = new Role();
        Associations.PlayerRole.addRole(player, role);
        Assertions.assertTrue(player.getRoles().contains(role));
        Assertions.assertTrue(role.getPlayers().contains(player));
    }

    @Test
    @Order(7)
    void testRemoveRole() {
        Player player = createPlayer();
        Role role = new Role();
        Associations.PlayerRole.addRole(player, role);
        Associations.PlayerRole.removeRole(player, role);
        Assertions.assertFalse(player.getRoles().contains(role));
        Assertions.assertFalse(role.getPlayers().contains(player));
    }

    @Test
    @Order(8)
    void testAddApiKey() {
        Player player = createPlayer();
        ApiKey apiKey = new ApiKey();
        Associations.PlayerApiKey.addApiKey(player, apiKey);
        Assertions.assertEquals(player.getApiKey(), apiKey);
        Assertions.assertEquals(apiKey.getPlayer(), player);
    }

    @Test
    @Order(9)
    void testRemoveApiKey() {
        Player player = createPlayer();
        ApiKey apiKey = new ApiKey();
        Associations.PlayerApiKey.addApiKey(player, apiKey);
        Associations.PlayerApiKey.removeApiKey(player, apiKey);
        Assertions.assertNull(player.getApiKey());
        Assertions.assertNull(apiKey.getPlayer());
    }

    @Test
    @Order(9)
    void testAddAccessLog() {
        ApiKey apiKey = new ApiKey();
        RestApiAccessLog accessLog = new RestApiAccessLog();
        Associations.ApiKeyAccessLog.addAccessLog(apiKey, accessLog);
        Assertions.assertTrue(apiKey.getAccessLogs().contains(accessLog));
        Assertions.assertEquals(accessLog.getApiKey(), apiKey);
    }

    @Test
    @Order(10)
    void testRemoveAccessLog() {
        ApiKey apiKey = new ApiKey();
        RestApiAccessLog accessLog = new RestApiAccessLog();
        Associations.ApiKeyAccessLog.addAccessLog(apiKey, accessLog);
        Associations.ApiKeyAccessLog.removeAccessLog(apiKey, accessLog);
        Assertions.assertFalse(apiKey.getAccessLogs().contains(accessLog));
        Assertions.assertNull(accessLog.getApiKey());
    }

    @Test
    @Order(11)
    void testAddGameSession() {
        Player player = createPlayer();
        GameSession gameSession = new GameSession();
        Associations.PlayerGameSession.addGameSession(player, gameSession);
        Assertions.assertTrue(player.getGameSessions().contains(gameSession));
        Assertions.assertEquals(gameSession.getPlayer(), player);
    }

    @Test
    @Order(12)
    void testRemoveGameSession() {
        Player player = createPlayer();
        GameSession gameSession = new GameSession();
        Associations.PlayerGameSession.addGameSession(player, gameSession);
        Associations.PlayerGameSession.removeGameSession(player, gameSession);
        Assertions.assertFalse(player.getGameSessions().contains(gameSession));
        Assertions.assertNull(gameSession.getPlayer());
    }

    @Test
    @Order(13)
    void testAddAnswer() {
        Question question = new Question();
        List<Answer> answers = new ArrayList<>();
        Answer answer1 = new Answer();
        Answer answer2 = new Answer();
        answers.add(answer1);
        answers.add(answer2);
        Associations.QuestionAnswers.addAnswer(question, answers);
        Assertions.assertTrue(question.getOptions().contains(answer1));
        Assertions.assertTrue(question.getOptions().contains(answer2));
        Assertions.assertEquals(answer1.getQuestion(), question);
        Assertions.assertEquals(answer2.getQuestion(), question);
    }

    @Test
    @Order(14)
    void testRemoveAnswer() {
        Question question = new Question();
        List<Answer> answers = new ArrayList<>();
        Answer answer1 = new Answer();
        Answer answer2 = new Answer();
        answers.add(answer1);
        answers.add(answer2);
        Associations.QuestionAnswers.addAnswer(question, answers);
        Associations.QuestionAnswers.removeAnswer(question, answers);
        Assertions.assertFalse(question.getOptions().contains(answer1));
        Assertions.assertFalse(question.getOptions().contains(answer2));
        Assertions.assertNull(answer1.getQuestion());
        Assertions.assertNull(answer2.getQuestion());
    }

    @Test
    @Order(15)
    void testCategoryCreation() {
        Category category = new Category("Test Category", "This is a test category");
        Assertions.assertEquals("Test Category", category.getName());
        Assertions.assertEquals("This is a test category", category.getDescription());
    }

    @Test
    @Order(16)
    void testJsonGeneration() {
        Category category = new Category("Test Category", "This is a test category");
        JsonNode json = category.toJson();
        Assertions.assertEquals("Test Category", json.get("name").asText());
        Assertions.assertEquals("This is a test category", json.get("description").asText());
    }

    @Test
    @Order(17)
    void testQuestionAssociation() {
        Category category = new Category("Test Category", "This is a test category");
        Question question = new Question();
        question.setCategory(category);

        Set<Question> questions = new HashSet<>();
        questions.add(question);
        category.setQuestions(questions);

        Assertions.assertEquals(1, category.getQuestions().size());
        Assertions.assertTrue(category.getQuestions().contains(question));
    }

    @Test
    @Order(18)
    void testAnswerToJson() {
        Question question = new Question();
        question.setId(1L);

        Answer answer = new Answer("Sample Answer", true);
        answer.setQuestion(question);
        answer.setId(1L);

        JsonNode json = answer.toJson();

        Assertions.assertEquals(1L, json.get("id").asLong());
        Assertions.assertEquals("Sample Answer", json.get("text").asText());
        Assertions.assertTrue(json.get("correct").asBoolean());
        Assertions.assertEquals(1L, json.get("question").asLong());
    }

    @Test
    @Order(19)
    void testAddQuestion_Correct() {
        Player player = createPlayer();
        List<Question> questions = new ArrayList<>();
        GameSession gameSession = new GameSession(player, questions);

        int initialScore = gameSession.getScore();
        gameSession.addQuestion(true, 20);
        Assertions.assertEquals(initialScore + 30, gameSession.getScore());
        Assertions.assertEquals(1, gameSession.getCorrectQuestions());
        Assertions.assertEquals(1, gameSession.getTotalQuestions());
    }

    @Test
    @Order(20)
    void testAddQuestion_Incorrect() {
        Player player = createPlayer();
        List<Question> questions = new ArrayList<>();
        GameSession gameSession = new GameSession(player, questions);

        int initialScore = gameSession.getScore();
        gameSession.addQuestion(false, 20);
        Assertions.assertEquals(initialScore, gameSession.getScore());
        Assertions.assertEquals(0, gameSession.getCorrectQuestions());
        Assertions.assertEquals(1, gameSession.getTotalQuestions());
    }

    @Test
    @Order(20)
    void testAddAnsweredQuestion() {
        Player player = createPlayer();
        List<Question> questions = new ArrayList<>();
        Question question = new Question();
        questions.add(question);
        GameSession gameSession = new GameSession(player, questions);

        Assertions.assertTrue(gameSession.getQuestionsToAnswer().contains(question));
        Assertions.assertFalse(gameSession.getAnsweredQuestions().contains(question));
        gameSession.addAnsweredQuestion(question);
        Assertions.assertFalse(gameSession.getQuestionsToAnswer().contains(question));
        Assertions.assertTrue(gameSession.getAnsweredQuestions().contains(question));
    }

    @Test
    @Order(21)
    void testGetDuration() {
        LocalDateTime createdAt = LocalDateTime.of(2022, 1, 1, 10, 0); // Assuming game started at 10:00 AM
        LocalDateTime finishTime = LocalDateTime.of(2022, 1, 1, 10, 5); // Assuming game finished at 10:05 AM
        Player player = createPlayer();
        List<Question> questions = new ArrayList<>();
        GameSession gameSession = new GameSession(player, questions);
        gameSession.setCreatedAt(createdAt);
        gameSession.setFinishTime(finishTime);

        Assertions.assertEquals("00:05:00", gameSession.getDuration());
    }

    @Test
    @Order(22)
    void testPlayerToJson() {
        Role role1 = new Role("ROLE_USER");
        Role role2 = new Role("ROLE_ADMIN");

        Set<Role> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        Player player = createPlayer();
        player.setId(1L);
        player.setRoles(roles);

        GameSession gameSession = new GameSession(player, new ArrayList<>());
        gameSession.setId(0L);
        Set<GameSession> gameSessions = new HashSet<>();
        gameSessions.add(gameSession);


        player.setGameSessions(gameSessions);

        JsonNode json = player.toJson();

        Assertions.assertEquals(1L, json.get("id").asLong());
        Assertions.assertEquals("name", json.get("username").asText());
        Assertions.assertEquals("test@email.com", json.get("email").asText());

        ArrayNode rolesArray = (ArrayNode) json.get("roles");
        Assertions.assertEquals(2, rolesArray.size());

        ArrayNode gameSessionsArray = (ArrayNode) json.get("gameSessions");
        Assertions.assertEquals(1, gameSessionsArray.size());
        // Se verifica que la sesión de juego está presente en el JSON
        Assertions.assertEquals(gameSession.getId(), gameSessionsArray.get(0).get("id").asLong());
    }

    @Test
    @Order(23)
    void testAddOption() {
        Question question = new Question();
        Answer option = new Answer("Option A", false);
        question.addOption(option);
        Assertions.assertTrue(question.getOptions().contains(option));
    }

    @Test
    @Order(24)
    void testRemoveOption() {
        Question question = new Question();
        Answer option = new Answer("Option A", false);
        question.addOption(option);
        question.removeOption(option);
        Assertions.assertFalse(question.getOptions().contains(option));
    }

    @Test
    @Order(25)
    void testScrambleOptions() {
        Question question = new Question();
        Answer option1 = new Answer("Option A", false);
        Answer option2 = new Answer("Option B", false);
        Answer option3 = new Answer("Option C", false);
        question.addOption(option1);
        question.addOption(option2);
        question.addOption(option3);

        List<Answer> scrambledOptions = question.returnScrambledOptions();

        Assertions.assertTrue(scrambledOptions.contains(option1));
        Assertions.assertTrue(scrambledOptions.contains(option2));
        Assertions.assertTrue(scrambledOptions.contains(option3));
    }

    @Test
    @Order(26)
    void testHasEmptyOptions() {
        Question question = new Question();
        Answer option1 = new Answer("Option A", false);
        Answer option2 = new Answer("", false); // Empty option
        question.addOption(option1);
        question.addOption(option2);

        Assertions.assertTrue(question.hasEmptyOptions());
    }

    @Test
    @Order(27)
    void testToJson() {
        Category category = new Category("Category", "Description");

        List<Answer> options = new ArrayList<>();
        Answer option1 = new Answer("Option A", false);
        Answer option2 = new Answer("Option B", false);
        options.add(option1);
        options.add(option2);

        Question question = new Question("Sample question", options, option1, category, "en");
        question.setId(1L);

        JsonNode json = question.toJson();

        Assertions.assertTrue(json.toString().contains("Sample question"));
        Assertions.assertTrue(json.toString().contains("Category"));
        Assertions.assertTrue(json.toString().contains("Option A"));
        Assertions.assertTrue(json.toString().contains("Option B"));
    }

    @Test
    @Order(28)
    void testGetPlayerNoApiKey() throws IOException, InterruptedException, JSONException {
        HttpResponse<String> response = sendRequest("GET", "/api/players", Map.of(), Map.of());

        Assertions.assertEquals(401, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertEquals("Invalid API key", json.getString("error"));
    }

    @Test
    @Order(29)
    void testGetPlayerInvalidApiKey() throws IOException, InterruptedException, JSONException {
        HttpResponse<String> response = sendRequest("GET", "/api/players", Map.of("API-KEY", "zzzz"), Map.of());

        Assertions.assertEquals(401, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertEquals("Invalid API key", json.getString("error"));
    }

    @Test
    @Order(30)
    void testGetAllPlayers() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/players", Map.of(), Map.of("apiKey", apiKey.getKeyToken()));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.has("players"));
        Assertions.assertTrue(json.getJSONArray("players").length() > 0);
    }

    @Test
    @Order(31)
    void testGetPlayerById() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/players", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(),
                        "id", String.valueOf(player.getId())));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        JSONObject playerJson = json.getJSONArray("players").getJSONObject(0);
        Assertions.assertEquals(player.getId(), playerJson.getLong("id"));
        Assertions.assertEquals(player.getUsername(), playerJson.getString("username"));
        Assertions.assertEquals(player.getEmail(), playerJson.getString("email"));
    }

    @Test
    @Order(32)
    void testGetPlayerByEmail() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/players", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(),
                        "email", player.getEmail()));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        JSONObject playerJson = json.getJSONArray("players").getJSONObject(0);
        Assertions.assertEquals(player.getId(), playerJson.getLong("id"));
        Assertions.assertEquals(player.getUsername(), playerJson.getString("username"));
        Assertions.assertEquals(player.getEmail(), playerJson.getString("email"));
    }

    @Test
    @Order(33)
    void testGetPlayerByUsername() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/players", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(),
                        "username", player.getUsername()));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        JSONObject playerJson = json.getJSONArray("players").getJSONObject(0);
        Assertions.assertEquals(player.getId(), playerJson.getLong("id"));
        Assertions.assertEquals(player.getUsername(), playerJson.getString("username"));
        Assertions.assertEquals(player.getEmail(), playerJson.getString("email"));
    }

    @Test
    @Order(34)
    void testGetPlayersByUsernames() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/players", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(),
                        "usernames", player.getUsername()));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        JSONArray players = json.getJSONArray("players");
        Assertions.assertTrue(players.length() > 0);
        for (int i = 0; i < players.length(); i++) {
            JSONObject playerJson = players.getJSONObject(i);
            Assertions.assertEquals(player.getUsername(), playerJson.getString("username"));
        }
    }

    @Test
    @Order(35)
    void testGetPlayersByEmails() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/players", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(),
                        "emails", player.getEmail()));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        JSONArray players = json.getJSONArray("players");
        Assertions.assertTrue(players.length() > 0);
        for (int i = 0; i < players.length(); i++) {
            JSONObject playerJson = players.getJSONObject(i);
            Assertions.assertEquals(player.getEmail(), playerJson.getString("email"));
        }
    }

    @Test
    @Order(35)
    void testGetPlayersByEmailsAndRole() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/players", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(),
                        "emails", player.getEmail(), "role", "ROLE_USER"));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        JSONArray players = json.getJSONArray("players");
        Assertions.assertTrue(players.length() > 0);
        for (int i = 0; i < players.length(); i++) {
            JSONObject playerJson = players.getJSONObject(i);
            Assertions.assertEquals(player.getEmail(), playerJson.getString("email"));
        }
    }

    @Test
    @Order(35)
    void testGetPlayersByRole() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/players", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(),
                        "role", "ROLE_USER"));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        JSONArray players = json.getJSONArray("players");
        Assertions.assertTrue(players.length() > 0);
        for (int i = 0; i < players.length(); i++) {
            JSONObject playerJson = players.getJSONObject(i);
            Assertions.assertEquals(player.getEmail(), playerJson.getString("email"));
        }
    }

    @Test
    @Order(36)
    void testCreatePlayerEmptyApiKey() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("POST", "/api/players", Map.of(),
                Map.of());

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(37)
    void testCreatePlayerInvalidApiKey() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("POST", "/api/players", Map.of("API-KEY", "zzzz"),
                Map.of());

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(38)
    void testCreatePlayerValid() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        Map<String, Object> data = new HashMap<>();

        data.put("username", "newUser");
        data.put("email", "newUser@email.com");
        data.put("password", "password");
        data.put("roles", new String[]{"ROLE_USER"});

        HttpResponse<String> response = sendRequest("POST", "/api/players", Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.getBoolean("success"));
        Long newId = json.getLong("id");

        Optional<Player> newPlayer = playerService.getUser(newId);
        Assertions.assertTrue(newPlayer.isPresent());
        Assertions.assertEquals("newUser", newPlayer.get().getUsername());
        Assertions.assertEquals("newUser@email.com", newPlayer.get().getEmail());

        playerService.deletePlayer(newId);
    }

    @Test
    @Order(39)
    void testCreateUserInvalidUsernameAndEmail() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        Map<String, Object> data = new HashMap<>();

        data.put("username", player.getUsername());
        data.put("email", player.getEmail());
        data.put("password", "password");
        data.put("roles", new String[]{"ROLE_USER"});

        HttpResponse<String> response = sendRequest("POST", "/api/players", Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertEquals(400, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.has("email"));
        Assertions.assertTrue(json.has("username"));
    }

    @Test
    @Order(40)
    void testCreateUserInvalidEmail() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        Map<String, Object> data = new HashMap<>();

        data.put("username", "user1");
        data.put("email", "notavalidemail");
        data.put("password", "password");
        data.put("roles", new String[]{"ROLE_USER"});

        HttpResponse<String> response = sendRequest("POST", "/api/players", Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertEquals(400, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.has("email"));
    }

    @Test
    @Order(41)
    void testModifyUser() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        Map<String, Object> data = new HashMap<>();
        data.put("username", "newUsername");
        data.put("email", "newEmail@email.com");
        data.put("password", "newPassword");
        data.put("roles", new String[]{"ROLE_USER"});

        HttpResponse<String> response = sendRequest("PATCH", "/api/players/" + player.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.getBoolean("success"));

        Optional<Player> updatedPlayer = playerService.getUser(player.getId());
        Assertions.assertTrue(updatedPlayer.isPresent());
        Assertions.assertEquals("newUsername", updatedPlayer.get().getUsername());
        Assertions.assertEquals("newEmail@email.com", updatedPlayer.get().getEmail());
    }

    @Test
    @Order(42)
    void testModifyInvalidApiKey() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();

        HttpResponse<String> response = sendRequest("PATCH", "/api/players/" + player.getId(), Map.of("API-KEY", "zzzz"),
                Map.of());

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(43)
    void testModifyUserAlreadyExisting() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        Map<String, Object> data = new HashMap<>();
        data.put("username", "test");
        data.put("email", "test@test.com");
        data.put("password", "newPassword");
        data.put("roles", new String[]{"ROLE_USER"});

        HttpResponse<String> response = sendRequest("PATCH", "/api/players/" + player.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertNotEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);

        Assertions.assertTrue(json.has("email"));
        Assertions.assertTrue(json.has("username"));
    }

    @Test
    @Order(44)
    void testModifyUserMissing() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        Map<String, Object> data = new HashMap<>();

        HttpResponse<String> response = sendRequest("PATCH", "/api/players/" + player.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(45)
    void testModifyUserMissingSomeData() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        Map<String, Object> data = new HashMap<>();
        data.put("username", "test");
        //data.put("email", "test@test.com"); // Missing email
        data.put("password", "newPassword");
        data.put("roles", new String[]{"ROLE_USER"});

        HttpResponse<String> response = sendRequest("PATCH", "/api/players/" + player.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(46)
    void testDeleteUserInvalidApiKey() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();

        HttpResponse<String> response = sendRequest("DELETE", "/api/players/" + player.getId(), Map.of("API-KEY", "zzzz"),
                Map.of());

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(47)
    void testDeleteUserNotFound() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("DELETE", "/api/players/9999999", Map.of("API-KEY", apiKey.getKeyToken()),
                Map.of());

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @Order(48)
    void testDeleteUser() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("DELETE", "/api/players/" + player.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                Map.of());

        Assertions.assertEquals(200, response.statusCode());

        Optional<Player> deletedPlayer = playerService.getUser(player.getId());
        Assertions.assertTrue(deletedPlayer.isEmpty());
    }

    @Test
    @Order(49)
    void testGetQuestionsInvalidApiKey() throws IOException, InterruptedException, JSONException {
        HttpResponse<String> response = sendRequest("GET", "/api/questions", Map.of("API-KEY", "zzzz"), Map.of());

        Assertions.assertEquals(401, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertEquals("Invalid API key", json.getString("error"));
    }

    @Test
    @Order(50)
    void testGetQuestions() throws IOException, InterruptedException, JSONException {
        insertSomeQuestions();
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/questions", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(), "lang", "es"));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.has("questions"));
        Assertions.assertTrue(json.getJSONArray("questions").length() > 0);
    }

    @Test
    @Order(50)
    void testGetQuestionsInvalidId() throws IOException, InterruptedException, JSONException {
        insertSomeQuestions();
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/questions", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(), "id", "notnumeric"));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.has("questions"));
        Assertions.assertEquals(0, json.getJSONArray("questions").length());
    }

    @Test
    @Order(51)
    void testGetQuestionsByCategoryName() throws IOException, InterruptedException, JSONException {
        String cat = "Science";
        questionGeneratorService.generateTestQuestions(cat);
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("GET", "/api/questions", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(), "category", cat));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.has("questions"));
        Assertions.assertTrue(json.getJSONArray("questions").length() > 0);
    }

    @Test
    @Order(52)
    void testGetQuestionsByCategoryId() throws IOException, InterruptedException, JSONException {
        String category = "Science";
        questionGeneratorService.generateTestQuestions(category);
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Category cat = categoryService.getCategoryByName(category);

        HttpResponse<String> response = sendRequest("GET", "/api/questions", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(), "category", cat.getId()));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.has("questions"));
        Assertions.assertTrue(json.getJSONArray("questions").length() > 0);
    }

    @Test
    @Order(53)
    void testGetQuestionById() throws IOException, InterruptedException, JSONException {
        insertSomeQuestions();
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Question question = questionService.getAllQuestions().getFirst();

        HttpResponse<String> response = sendRequest("GET", "/api/questions", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(),
                        "id", question.getId()));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        JSONObject questionJson = json.getJSONArray("questions").getJSONObject(0);
        Assertions.assertEquals(question.getId(), questionJson.getLong("id"));
        Assertions.assertEquals(question.getStatement(), questionJson.getString("statement"));
    }

    @Test
    @Order(53)
    void testGetQuestionByStatement() throws IOException, InterruptedException, JSONException {
        insertSomeQuestions();
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Question question = questionService.getAllQuestions().getFirst();

        HttpResponse<String> response = sendRequest("GET", "/api/questions", Map.of(),
                Map.of("apiKey", apiKey.getKeyToken(),
                        "statement", question.getStatement()));

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        JSONObject questionJson = json.getJSONArray("questions").getJSONObject(0);
        Assertions.assertEquals(question.getId(), questionJson.getLong("id"));
        Assertions.assertEquals(question.getStatement(), questionJson.getString("statement"));
    }

    @Test
    @Order(54)
    void PlayerServiceImpl_addNewPlayer_UsedEmail() {
        PlayerDto p1 = new PlayerDto("a", "abcd@gmail.com", "1221", "1221", null);
        playerService.addNewPlayer(p1);

        PlayerDto dto = new PlayerDto("b", "abcd@gmail.com", "1221", "1221", null);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> playerService.addNewPlayer(dto));
        Assertions.assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    @Order(55)
    void PlayerServiceImpl_addNewPlayer_UsedUsername() {
        PlayerDto p1 = new PlayerDto("a", "abcd@gmail.com", "1221", "1221", null);
        playerService.addNewPlayer(p1);

        PlayerDto dto = new PlayerDto("a", "a@gmail.com", "1221", "1221", null);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> playerService.addNewPlayer(dto));
        Assertions.assertEquals("Username already in use", exception.getMessage());
    }

    @Test
    @Order(56)
    void PlayerServiceImpl_addNewPlayer_AddedCorrectly() {
        PlayerDto dto = new PlayerDto("a", "a@gmail.com", "1221", "1221", null);

        Player player = playerService.addNewPlayer(dto);

        Assertions.assertNotNull(player);
        Assertions.assertEquals(dto.getUsername(), player.getUsername());
        Assertions.assertEquals(dto.getEmail(), player.getEmail());
        Assertions.assertTrue(passwordEncoder.matches(dto.getPassword(), player.getPassword()));
    }

    @Test
    @Order(57)
    void PlayerServiceImpl_addNewPlayer_RoleExists() {
        PlayerDto dto = new PlayerDto("a", "a@gmail.com", "1221", "1221", new String[]{"ROLE_USER"});
        roleService.addRole(new RoleDto(dto.getRoles()[0]));

        Player player = playerService.addNewPlayer(dto);

        Assertions.assertNotNull(player);
        Assertions.assertEquals(dto.getUsername(), player.getUsername());
        Assertions.assertEquals(dto.getEmail(), player.getEmail());
        Assertions.assertTrue(passwordEncoder.matches(dto.getPassword(), player.getPassword()));
    }

    @Test
    @Order(58)
    void PlayerServiceImpl_getUsers_ReturnsPlayersList() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("test", "test@test.com", "1a"));
        players.add(new Player("a", "a@gmail.com", "1a"));
        players.add(new Player("b", "b@gmail.com", "1b"));

        playerRepository.save(new Player("a", "a@gmail.com", "1a"));
        playerRepository.save(new Player("b", "b@gmail.com", "1b"));

        List<Player> result = playerService.getUsers();

        Assertions.assertEquals(players.size(), result.size());
        for (int i = 0; i < players.size(); i++) {
            Assertions.assertEquals(players.get(i).getEmail(), result.get(i).getEmail());
            Assertions.assertEquals(players.get(i).getUsername(), result.get(i).getUsername());
        }
    }

    @Test
    @Order(59)
    void PlayerServiceImpl_getUsers_ReturnsEmptyList() {
        List<Player> result = playerService.getUsers();

        // Always exists 1 test user
        Assertions.assertEquals(1, result.size());
    }

    @Test
    @Order(60)
    void PlayerServiceImpl_getUserByEmail_ReturnsPlayer() {
        String email = "a@gmail.com";
        Player player = new Player("a", email, "password");

        playerRepository.save(player);

        Optional<Player> result = playerService.getUserByEmail(email);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(player.getEmail(), result.get().getEmail());
        Assertions.assertEquals(player.getUsername(), result.get().getUsername());
    }

    @Test
    @Order(8)
    void PlayerServiceImpl_getUserByEmail_ReturnsEmpty() {
        String email = "nonexist@gmail.com";

        Optional<Player> result = playerService.getUserByEmail(email);

        Assertions.assertEquals(Optional.empty(), result);
    }

    @Test
    @Order(61)
    void PlayerServiceImpl_getUserByUsername_ReturnsPlayer() {
        String username = "abc";
        Player player = new Player(username, "a@gmail,com", "password");

        playerRepository.save(player);

        Optional<Player> result = playerService.getUserByUsername(username);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(player.getUsername(), result.get().getUsername());
        Assertions.assertEquals(player.getEmail(), result.get().getEmail());
    }

    @Test
    @Order(62)
    void PlayerServiceImpl_getUserByUsername_ReturnsEmpty() {
        String username = "nonexist";

        Optional<Player> result = playerService.getUserByUsername(username);

        Assertions.assertEquals(Optional.empty(), result);
    }

    @Test
    @Order(63)
    void AnswerServiceImpl_addNewAnswer_SavesAnswer() {
        Answer respuesta = new Answer("respuesta", true);

        answerService.addNewAnswer(respuesta);

        Optional<Answer> respuestaGuardada = answerRepository.findById(respuesta.getId());
        Assertions.assertTrue(respuestaGuardada.isPresent());
        Assertions.assertEquals(respuesta.getText(), respuestaGuardada.get().getText());
        Assertions.assertEquals(respuesta.isCorrect(), respuestaGuardada.get().isCorrect());
    }

    @Test
    @Order(64)
    void AnswerServiceImpl_getAnswersPerQuestion_QuestionExists() {
        String statement = "What is the capital of France?";
        List<Answer> options = new ArrayList<>();
        options.add(new Answer("Paris", true));
        options.add(new Answer("Madrid", false));
        options.add(new Answer("Rome", false));
        Answer correctAnswer = options.getFirst();
        Category category = new Category("Geography_Capitales", "Capitales mundiales");
        categoryRepository.save(category);

        String language = "en";
        Question question = new Question(statement, options, correctAnswer, category, language);

        List<Answer> expectedAnswers = question.getOptions();
        questionService.addNewQuestion(question);
        // Act
        List<Answer> result = answerService.getAnswersPerQuestion(question);

        // Assert
        Assertions.assertEquals(expectedAnswers.size(), result.size());
        Assertions.assertEquals(3, result.size());
    }

    @Test
    @Order(65)
    void AnswerServiceImpl_getAnswer_ReturnsEmpty() {
        Long id = 999L;

        Optional<Answer> result = answerService.getAnswer(id);

        Assertions.assertEquals(Optional.empty(), result);
    }

    @Test
    @Order(66)
    void AnswerServiceImpl_getAnswer_ReturnsAnswer() {
        Answer answer = new Answer("Content", true);
        Long id = answerRepository.save(answer).getId();

        Optional<Answer> result = answerService.getAnswer(id);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(answer.getText(), result.get().getText());
        Assertions.assertEquals(answer.isCorrect(), result.get().isCorrect());
    }

    @Test
    @Order(67)
    void CategoryServiceImpl_addNewCategory_SavesCategory() {
        Category category = new Category("Capitals", "Capitals from countries");

        categoryService.addNewCategory(category);

        Optional<Category> savedCategory = categoryRepository.findById(category.getId());
        Assertions.assertTrue(savedCategory.isPresent());
        Assertions.assertEquals(category.getName(), savedCategory.get().getName());
        Assertions.assertEquals(category.getDescription(), savedCategory.get().getDescription());
    }

    @Test
    @Order(68)
    void CategoryServiceImpl_getAllCategories_ReturnsList() {
        List<Category> categories = new ArrayList<>();
        Category category = new Category("Capitals", "Capitals from countries");
        Category category2 = new Category("Geography", "Questions about geography");
        categories.add(category2);
        categories.add(category);

        categoryService.addNewCategory(category);

        List<Category> result = categoryService.getAllCategories();

        Assertions.assertEquals(categories.size(), result.size());
        for (int i = 0; i < categories.size(); i++) {
            Assertions.assertEquals(categories.get(i).getName(), result.get(i).getName());
            Assertions.assertEquals(categories.get(i).getDescription(), result.get(i).getDescription());
        }
    }

    @Test
    @Order(69)
    void CategoryServiceImpl_getAllCategories_EmptyList() {
        List<Category> result = categoryService.getAllCategories();

        Assertions.assertEquals(1, result.size());
    }

    @Test
    @Order(70)
    void CategoryServiceImpl_getCategory_ReturnsCategory() {
        Category category = new Category("Capitals", "Capitals from countries");

        categoryService.addNewCategory(category);

        Optional<Category> result = categoryService.getCategory(category.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(category.getName(), result.get().getName());
        Assertions.assertEquals(category.getDescription(), result.get().getDescription());
    }

    @Test
    @Order(71)
    void CategoryServiceImpl_getCategory_ReturnsEmptyOpt() {
        Long id = 999L;
        Optional<Category> result = categoryService.getCategory(id);

        Assertions.assertEquals(Optional.empty(), result);
    }

    @Test
    @Order(72)
    void CategoryServiceImpl_getCategoryByName_ReturnsCategory() {
        String name = "Capitals";
        Category category = new Category(name, "Capitals from countries");

        categoryService.addNewCategory(category);

        Category result = categoryService.getCategoryByName(name);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(category.getName(), result.getName());
        Assertions.assertEquals(category.getDescription(), result.getDescription());
    }

    @Test
    @Order(73)
    void CategoryServiceImpl_getCategoryByName_ReturnsNull() {
        Category result = categoryService.getCategoryByName("abcd");

        Assertions.assertNull(result);
    }

    @Test
    @Order(74)
    void GameSessionImpl_getGameSessions_ReturnsList() {
        List<GameSession> gameSessions = new ArrayList<>();
        GameSession gameSession1 = new GameSession();
        gameSessions.add(gameSession1);
        GameSession gameSession2 = new GameSession();
        gameSessions.add(gameSession2);

        gameSessionRepository.save(gameSession1);
        gameSessionRepository.save(gameSession2);

        List<GameSession> result = gameSessionService.getGameSessions();

        Assertions.assertEquals(gameSessions.size(), result.size());
    }

    @Test
    @Order(75)
    void GameSessionImpl_getGameSessions_ReturnsEmptyList() {
        List<GameSession> result = gameSessionService.getGameSessions();

        Assertions.assertEquals(0, result.size());
    }

    @Test
    @Order(76)
    void GameSessionImpl_getGameSessionsByPlayer_ReturnsList() {
        Player player = new Player("abc", "abc@gmail.com", "abcd1234");
        playerRepository.save(player);

        List<GameSession> gameSessions = new ArrayList<>();
        GameSession gameSession1 = new GameSession(player, new ArrayList<>());
        gameSessions.add(gameSession1);
        GameSession gameSession2 = new GameSession(player, new ArrayList<>());
        gameSessions.add(gameSession2);

        gameSessionRepository.save(gameSession1);
        gameSessionRepository.save(gameSession2);

        List<GameSession> result = gameSessionService.getGameSessionsByPlayer(player);

        Assertions.assertEquals(gameSessions.size(), result.size());
    }

    @Test
    @Order(77)
    void GameSessionImpl_getGameSessionsByPlayer_ReturnsEmptyList() {
        Player p = new Player("nonExists", "aabb@gmail.com", "abbacdc");
        playerRepository.save(p);

        List<GameSession> result = gameSessionService.getGameSessionsByPlayer(p);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    @Order(82)
    void testAddQuestionInvalidApiKey() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("POST", "/api/questions", Map.of("API-KEY", "zzzz"),
                Map.of());

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(83)
    void testAddQuestionMissingData() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("POST", "/api/questions", Map.of("API-KEY", apiKey.getKeyToken()),
                Map.of());

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(84)
    void testAddQuestion() throws IOException, InterruptedException, JSONException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Category category = categoryService.getCategoryByName("Geography");

        Map<String, Object> data = new HashMap<>();
        data.put("statement", "Sample question");

        List<Map<String, Object>> opts = new ArrayList<>();
        opts.add(Map.of("text", "Option A", "correct", true));
        opts.add(Map.of("text", "Option B", "correct", false));
        opts.add(Map.of("text", "Option C", "correct", false));
        opts.add(Map.of("text", "Option D", "correct", false));

        data.put("options", opts);
        data.put("category", Map.of("name", category.getName()));
        data.put("language", "en");

        HttpResponse<String> response = sendRequest("POST", "/api/questions", Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.getBoolean("success"));
        Long newId = json.getLong("id");

        Optional<Question> newQuestion = questionService.getQuestion(newId);
        Assertions.assertTrue(newQuestion.isPresent());
        Assertions.assertEquals("Sample question", newQuestion.get().getStatement());
        Assertions.assertEquals(4, newQuestion.get().getOptions().size());
        Assertions.assertTrue(newQuestion.get().getOptions().stream().anyMatch(Answer::isCorrect));
    }

    @Test
    @Order(85)
    void testAddQuestionWithLessOptions() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Category category = categoryService.getCategoryByName("Geography");

        Map<String, Object> data = new HashMap<>();
        data.put("statement", "Sample question");

        List<Map<String, Object>> opts = new ArrayList<>();
        opts.add(Map.of("text", "Option A", "correct", true));
        opts.add(Map.of("text", "Option B", "correct", false));

        data.put("options", opts);
        data.put("category", Map.of("name", category.getName()));
        data.put("language", "en");

        HttpResponse<String> response = sendRequest("POST", "/api/questions", Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(86)
    void testAddQuestionWithNoCorrect() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Category category = categoryService.getCategoryByName("Geography");

        Map<String, Object> data = new HashMap<>();
        data.put("statement", "Sample question");

        List<Map<String, Object>> opts = new ArrayList<>();
        opts.add(Map.of("text", "Option A", "correct", false));
        opts.add(Map.of("text", "Option B", "correct", false));
        opts.add(Map.of("text", "Option C", "correct", false));
        opts.add(Map.of("text", "Option D", "correct", false));

        data.put("options", opts);
        data.put("category", Map.of("name", category.getName()));
        data.put("language", "en");

        HttpResponse<String> response = sendRequest("POST", "/api/questions", Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(87)
    void testAddQuestionMultipleCorrect() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Category category = categoryService.getCategoryByName("Geography");

        Map<String, Object> data = new HashMap<>();
        data.put("statement", "Sample question");

        List<Map<String, Object>> opts = new ArrayList<>();
        opts.add(Map.of("text", "Option A", "correct", true));
        opts.add(Map.of("text", "Option B", "correct", true));
        opts.add(Map.of("text", "Option C", "correct", false));
        opts.add(Map.of("text", "Option D", "correct", false));

        data.put("options", opts);
        data.put("category", Map.of("name", category.getName()));
        data.put("language", "en");

        HttpResponse<String> response = sendRequest("POST", "/api/questions", Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(88)
    void testModifyQuestionInvalidApiKey() throws IOException, InterruptedException {
        insertSomeQuestions();
        Question question = questionService.getAllQuestions().getFirst();

        HttpResponse<String> response = sendRequest("PATCH", "/api/questions/" + question.getId(), Map.of("API-KEY", "zzzz"),
                Map.of());

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(89)
    void testModifyQuestionNotFound() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("PATCH", "/api/questions/9999999", Map.of("API-KEY", apiKey.getKeyToken()),
                Map.of());

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @Order(90)
    void testModifyQuestionMissingData() throws IOException, InterruptedException {
        insertSomeQuestions();

        Question question = questionService.getAllQuestions().getFirst();
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("PATCH", "/api/questions/" + question.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                Map.of());

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(91)
    void testModifyQuestion() throws IOException, InterruptedException, JSONException {
        insertSomeQuestions();
        Question question = questionService.getAllQuestions().getFirst();
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Category category = categoryService.getCategoryByName("Geography");

        Map<String, Object> data = new HashMap<>();
        data.put("statement", "Modified question");

        List<Map<String, Object>> opts = new ArrayList<>();
        opts.add(Map.of("text", "Option A", "correct", true));
        opts.add(Map.of("text", "Option B", "correct", false));
        opts.add(Map.of("text", "Option C", "correct", false));
        opts.add(Map.of("text", "Option D", "correct", false));

        data.put("options", opts);
        data.put("category", Map.of("name", category.getName()));
        data.put("language", "en");

        HttpResponse<String> response = sendRequest("PATCH", "/api/questions/" + question.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.getBoolean("success"));

        Optional<Question> updatedQuestion = questionService.getQuestion(question.getId());
        Assertions.assertTrue(updatedQuestion.isPresent());
        Assertions.assertEquals("Modified question", updatedQuestion.get().getStatement());
    }

    @Test
    @Order(91)
    void testModifyQuestionNewCategory() throws IOException, InterruptedException, JSONException {
        insertSomeQuestions();
        Question question = questionService.getAllQuestions().getFirst();
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Category category = categoryService.getCategoryByName("Geography");

        Map<String, Object> data = new HashMap<>();
        data.put("statement", "Modified question");

        List<Map<String, Object>> opts = new ArrayList<>();
        opts.add(Map.of("text", "Option A", "correct", true));
        opts.add(Map.of("text", "Option B", "correct", false));
        opts.add(Map.of("text", "Option C", "correct", false));
        opts.add(Map.of("text", "Option D", "correct", false));

        data.put("options", opts);
        data.put("category", Map.of("name", "NewCreatedCategory"));
        data.put("language", "en");

        HttpResponse<String> response = sendRequest("PATCH", "/api/questions/" + question.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertEquals(200, response.statusCode());
        JSONObject json = parseJsonResponse(response);
        Assertions.assertTrue(json.getBoolean("success"));

        Optional<Question> updatedQuestion = questionService.getQuestion(question.getId());
        Assertions.assertTrue(updatedQuestion.isPresent());
        Assertions.assertEquals("Modified question", updatedQuestion.get().getStatement());
    }

    @Test
    @Order(92)
    void testModifyQuestionWithLessOptions() throws IOException, InterruptedException {
        insertSomeQuestions();
        Question question = questionService.getAllQuestions().getFirst();
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Category category = categoryService.getCategoryByName("Geography");

        Map<String, Object> data = new HashMap<>();
        data.put("statement", "Modified question");

        List<Map<String, Object>> opts = new ArrayList<>();
        opts.add(Map.of("text", "Option A", "correct", true));
        opts.add(Map.of("text", "Option B", "correct", false));

        data.put("options", opts);
        data.put("category", Map.of("name", category.getName()));
        data.put("language", "en");

        HttpResponse<String> response = sendRequest("PATCH", "/api/questions/" + question.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(93)
    void testModifyQuestionWithNoCorrect() throws IOException, InterruptedException {
        insertSomeQuestions();
        Question question = questionService.getAllQuestions().getFirst();
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();
        Category category = categoryService.getCategoryByName("Geography");

        Map<String, Object> data = new HashMap<>();
        data.put("statement", "Modified question");

        List<Map<String, Object>> opts = new ArrayList<>();
        opts.add(Map.of("text", "Option A", "correct", false));
        opts.add(Map.of("text", "Option B", "correct", false));
        opts.add(Map.of("text", "Option C", "correct", false));
        opts.add(Map.of("text", "Option D", "correct", false));

        data.put("options", opts);
        data.put("category", Map.of("name", category.getName()));
        data.put("language", "en");

        HttpResponse<String> response = sendRequest("PATCH", "/api/questions/" + question.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                data);

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(94)
    void testDeleteQuestionInvalidApiKey() throws IOException, InterruptedException {
        insertSomeQuestions();
        Question question = questionService.getAllQuestions().getFirst();

        HttpResponse<String> response = sendRequest("DELETE", "/api/questions/" + question.getId(), Map.of("API-KEY", "zzzz"),
                Map.of());

        Assertions.assertNotEquals(200, response.statusCode());
    }

    @Test
    @Order(95)
    void testDeleteQuestionNotFound() throws IOException, InterruptedException {
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("DELETE", "/api/questions/9999999", Map.of("API-KEY", apiKey.getKeyToken()),
                Map.of());

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @Order(96)
    @Tag("flaky")
    void testDeleteQuestion() throws IOException, InterruptedException {
        insertSomeQuestions();
        Question question = questionService.getAllQuestions().getFirst();
        Player player = playerService.getUsersByRole("ROLE_USER").getFirst();
        ApiKey apiKey = player.getApiKey();

        HttpResponse<String> response = sendRequest("DELETE", "/api/questions/" + question.getId(), Map.of("API-KEY", apiKey.getKeyToken()),
                Map.of());

        Assertions.assertEquals(200, response.statusCode());
        Optional<Question> deletedQuestion = questionService.getQuestion(question.getId());
        Assertions.assertTrue(deletedQuestion.isEmpty());
    }

    @Test
    @Order(97)
    void testGetPlayersWithScores() {
        Player player1 = playerRepository.save(createDiferentPlayer("aa"));
        Player player2 = playerRepository.save(createDiferentPlayer("bb"));
        Player player3 = playerRepository.save(createDiferentPlayer("cc"));

        MultiplayerSession session = new MultiplayerSession("123", player3);
        Map<Player, Integer> playerScores = new HashMap<>();
        playerScores.put(player1, 10);
        playerScores.put(player2, 5);
        session.setPlayerScores(playerScores);

        multiplayerSessionRepository.save(session);

        Map<Player, Integer> result = multiplayerSessionService.getPlayersWithScores(123);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    @Order(99)
    void testAddToLobby() {

        Long playerId = 1L;
        Player player = createPlayer();
        player.setId(playerId);

        String code = "123";
        MultiplayerSession multiplayerSession = new MultiplayerSession();
        multiplayerSession.setMultiplayerCode(code);

        playerRepository.save(player);
        multiplayerSessionRepository.save(multiplayerSession);

        multiplayerSessionService.addToLobby(code, playerId);

        MultiplayerSession ms = multiplayerSessionRepository.findByMultiplayerCode(code);
        Assertions.assertNotNull(ms);
        Assertions.assertEquals("123", ms.getMultiplayerCode());
    }

    @Test
    @Order(100)
    void testChangeScore() {
        Long playerId = 1L;
        Player player = createPlayer();
        player.setId(playerId);

        String code = "123";
        int newScore = 100;
        MultiplayerSession multiplayerSession = new MultiplayerSession();
        multiplayerSession.setMultiplayerCode(code);
        multiplayerSession.addPlayer(player);

        playerRepository.save(player);
        multiplayerSessionRepository.save(multiplayerSession);

        multiplayerSessionService.changeScore(code, playerId, newScore);

        multiplayerSession = multiplayerSessionRepository.findByMultiplayerCode("123");
        Assertions.assertNotNull(multiplayerSession);
    }


    @Test
    @Order(101)
    void testMultiplayerIdGeneration() {
        MultiplayerSession multiplayerSession = new MultiplayerSession();
        multiplayerSessionRepository.save(multiplayerSession);

        Long id = multiplayerSession.getId();
        Assertions.assertNotNull(id);
    }


    @Test
    @Order(102)
    void testMultiplayerCodeInitialization() {
        Long playerId = 1L;
        Player player = createPlayer();
        player.setId(playerId);

        Integer multiplayerCode = player.getMultiplayerCode();
        Assertions.assertNull(multiplayerCode);
    }

    @Test
    @Order(103)
    void testScoreMultiplayerCodeInitialization() {
        Long playerId = 1L;
        Player player = createPlayer();
        player.setId(playerId);

        String scoreMultiplayerCode = player.getScoreMultiplayerCode();
        Assertions.assertNull(scoreMultiplayerCode);
    }

    @Test
    @Order(104)
    void testMultiplayerCodeAssignment() {
        Long playerId = 1L;
        Player player = createPlayer();
        player.setId(playerId);

        player.setMultiplayerCode(123);
        Integer multiplayerCode = player.getMultiplayerCode();
        Assertions.assertEquals(123, multiplayerCode);
    }

    @Test
    @Order(105)
    void testScoreMultiplayerCodeAssignment() {
        Long playerId = 1L;
        Player player = createPlayer();
        player.setId(playerId);

        player.setScoreMultiplayerCode("200");
        String scoreMultiplayerCode = player.getScoreMultiplayerCode();
        Assertions.assertEquals("200", scoreMultiplayerCode);
    }

    //@Test
    //@Order(106)
    //void GameSessionImpl_startNewMultiplayerGame()  throws InterruptedException, IOException {
    //    testQuestions(10);
//
    //    Long playerId = 1L;
    //    Player player = createPlayer();
    //    player.setId(playerId);
//
    //    String code = "123";
    //    playerRepository.findById(playerId);
    //    multiplayerSessionService.multiCreate(code, playerId);
//
    //    GameSession multiplayerGame = gameSessionService.startNewMultiplayerGame(player, 123);
//
    //    Assertions.assertNotNull(multiplayerGame);
    //    Assertions.assertEquals(player, multiplayerGame.getPlayer());
    //}

    //@Test
    //@Order(107)
    //void testRandomMultiplayerQuestions() throws InterruptedException, IOException {
    //    testQuestions(10);
//
    //    Long playerId = 1L;
    //    Player player = createPlayer();
    //    player.setId(playerId);
//
    //    String code = "123";
    //    playerRepository.findById(playerId);
    //    multiplayerSessionService.multiCreate(code, playerId);
//
//
    //    List<Question> questions = questionService.getRandomMultiplayerQuestions(5, 123);
//
    //    Assertions.assertEquals(5,questions.size());
    //}

    @Test
    @Order(108)
    void PlayerServiceImpl_getUsersByMultiplayerCode_ReturnsPlayer() {
        Long playerId = 1L;
        Player player = createPlayer();
        player.setId(playerId);

        player.setMultiplayerCode(123);
        playerRepository.save(player);

        List<Player> result = playerService.getUsersByMultiplayerCode(123);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(123, result.getFirst().getMultiplayerCode());
    }

    @Test
    @Order(109)
    void PlayerServiceImpl_getUsersByMultiplayerCode_ReturnsEmpty() {
        List<Player> result = playerService.getUsersByMultiplayerCode(123);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Order(110)
    void PlayerServiceImpl_setANDgetScoreMultiplayerCode() {
        Long playerId = 1L;
        Player player = createPlayer();
        player.setId(playerId);

        player.setMultiplayerCode(123);
        playerRepository.save(player);

        String score = "100";
        playerService.setScoreMultiplayerCode(playerId, score);

        String result = playerService.getScoreMultiplayerCode(playerId);

        Assertions.assertEquals(score, result);
    }

    @Test
    @Order(111)
    void PlayerServiceImpl_setANDgetScoreMultiplayerCode_EmptyPlayer() {
        Long playerId = 5L;

        String score = "100";
        playerService.setScoreMultiplayerCode(playerId, score);

        String result = playerService.getScoreMultiplayerCode(playerId);

        Assertions.assertEquals("", result);
    }

    @Test
    @Order(112)
    void PlayerServiceImpl_createMultiplayerGame() {
        Long playerId = 1L;

        int result = playerService.createMultiplayerGame(playerId);
        Assertions.assertNotEquals(-1, result);
    }

    @Test
    @Order(113)
    void PlayerServiceImpl_createMultiplayerGame_EmptyPlayer() {
        Long playerId = 5L;

        int result = playerService.createMultiplayerGame(playerId);
        Assertions.assertEquals(-1, result);
    }

    @Test
    @Order(114)
    void PlayerServiceImpl_deleteMultiplayerCode() {
        Long playerId = 1L;
        Player player = createPlayer();
        player.setId(playerId);
        playerRepository.save(player);
        playerService.createMultiplayerGame(playerId);

        playerService.deleteMultiplayerCode(playerId);
        Assertions.assertNull(player.getMultiplayerCode());
    }

    @Test
    void testSendQuestionToLLM_Empathy() {
        AnswerImage a1 = new AnswerImage("Asturias",false);
        AnswerImage a2 = new AnswerImage("Cataluña",false);
        AnswerImage a3 = new AnswerImage("Madrid",false);
        AnswerImage a4 = new AnswerImage("Benidorm",true);
        List<AnswerImage> lanswer = Arrays.asList(a1, a2, a3, a4);
        QuestionImage questionImage =
                new QuestionImage("", lanswer, a4,
                        new Category(), "es","https://www.wikidata.org/wiki/Q487981#/media/File:Vista_de_Benidorm,_Espa%C3%B1a,_2014-07-02,_DD_67.JPG");
        String answer = questionImageService.getHintForImageQuestion(questionImage);
        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isEmpty());
        Assertions.assertFalse(answer.isBlank());
        System.out.println(answer);
    }


    /**
     * Sends an HTTP request to the API
     *
     * @param method  HTTP method
     * @param uri     URI to send the request to
     * @param headers Headers to include in the request
     * @param data    Data to send in the request
     * @return The response from the server
     * @throws IOException
     * @throws InterruptedException
     */
    private HttpResponse<String> sendRequest(String method, String uri,
                                             Map<String, String> headers,
                                             Map<String, Object> data) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

        uri = Wichat_IntegrationTests.URL.substring(0, Wichat_IntegrationTests.URL.length() - 1) + uri;

        if ("GET".equalsIgnoreCase(method)) {
            if (!data.isEmpty()) {
                uri += "?" + buildQueryString(data);
            }
            requestBuilder.uri(URI.create(uri)).GET();
        } else if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            JSONObject json = new JSONObject(data);
            requestBuilder.uri(URI.create(uri))
                    .method(method.toUpperCase(), HttpRequest.BodyPublishers.ofString(json.toString()))
                    .header("Content-Type", "application/json");
        } else if ("DELETE".equalsIgnoreCase(method)) {
            requestBuilder.uri(URI.create(uri)).DELETE();
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        headers.forEach(requestBuilder::header);

        HttpRequest request = requestBuilder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Builds a query string from a map of data
     *
     * @param data The data to include in the query string
     * @return The query string
     */
    private String buildQueryString(Map<String, Object> data) {
        StringJoiner sj = new StringJoiner("&");
        data.forEach((key, value) -> sj.add(URLEncoder.encode(key, StandardCharsets.UTF_8) + "="
                + URLEncoder.encode(value.toString(), StandardCharsets.UTF_8)));
        return sj.toString();
    }

    /**
     * Parses the JSON response from the server
     *
     * @param response The response from the server
     * @return The JSON object
     * @throws JSONException
     */
    private JSONObject parseJsonResponse(HttpResponse<String> response) throws JSONException {
        return new JSONObject(response.body());
    }

    /**
     * Inserts some sample questions into the database
     */
    private void insertSomeQuestions() throws IOException, InterruptedException {
        questionGeneratorService.generateTestQuestions();
    }
}
