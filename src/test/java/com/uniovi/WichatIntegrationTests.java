package com.uniovi;


import com.uniovi.entities.*;
import com.uniovi.services.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootTest
@Tag("unitIntegration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class WichatIntegrationTests {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private GameSessionService gameSessionService;
    @Autowired
    private InsertSampleDataService sampleDataService;
    @Autowired
    private MultiplayerSessionService multiplayerSessionService;
    @Autowired
    private QuestionGeneratorService questionGeneratorService;

    private final HttpClient httpClient = HttpClient.newHttpClient();


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
