package com.uniovi.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.dto.PlayerDto;
import com.uniovi.entities.*;
import com.uniovi.services.ApiKeyService;
import com.uniovi.services.PlayerService;
import com.uniovi.services.RestApiService;
import com.uniovi.validators.SignUpValidator;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SimpleErrors;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@OpenAPIDefinition(info = @Info(title = "Wikigame API", version = "1.0", description = "API for managing players and questions\nTo get access, please generate an API key in the webpage"),
servers = {
        @Server(url = "https://wikigame.es", description = "Production server"),
        @Server(url = "http://localhost:8080", description = "Local server"),
})
@Tag(name = "Player API", description = "API for managing players")
@RestController
public class PlayerApiController {
    private final ApiKeyService apiKeyService;
    private final RestApiService restApiService;
    private final SignUpValidator signUpValidator;
    private final PlayerService playerService;

    @Autowired
    public PlayerApiController(ApiKeyService apiKeyService, RestApiService restApiService, SignUpValidator signUpValidator, PlayerService playerService) {
        this.apiKeyService = apiKeyService;
        this.restApiService = restApiService;
        this.signUpValidator = signUpValidator;
        this.playerService = playerService;
    }

    @Operation(summary = "Get players by various filters", description = "Fetch players based on the provided parameters such as username, email, id, roles.")
    @Parameters({
            @Parameter(name = "apiKey", description = "API key for authentication", required = true),
            @Parameter(name = "username", description = "Username of the player"),
            @Parameter(name = "email", description = "Email of the player"),
            @Parameter(name = "id", description = "ID of the player"),
            @Parameter(name = "usernames", description = "Comma-separated list of usernames"),
            @Parameter(name = "emails", description = "Comma-separated list of emails"),
            @Parameter(name = "role", description = "Role of the player. Will return players that have this role."),
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "Example response",
                                    value = "{\n" +
                                            "   \"players\":[\n" +
                                            "      {\n" +
                                            "         \"id\":1,\n" +
                                            "         \"username\":\"student1\",\n" +
                                            "         \"email\":\"student1@example.com\",\n" +
                                            "         \"roles\":[\n" +
                                            "            \"ROLE_USER\"\n" +
                                            "         ],\n" +
                                            "         \"gameSessions\":[\n" +
                                            "            {\n" +
                                            "               \"id\":1,\n" +
                                            "               \"player\":1,\n" +
                                            "               \"correctQuestions\":10,\n" +
                                            "               \"totalQuestions\":40,\n" +
                                            "               \"createdAt\":\"2024-03-04T22:44:41.067901\",\n" +
                                            "               \"finishTime\":\"2024-03-04T22:49:41.067901\",\n" +
                                            "               \"score\":0\n" +
                                            "            }\n" +
                                            "         ]\n" +
                                            "      }\n" +
                                            "   ]\n" +
                                            "}")}
                    )}),
            @ApiResponse(responseCode = "401", description = "Unauthorized if invalid api key",
                    content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response",
                            value = "{\"error\":\"Invalid API key\"}"
                    )}))
    })
    @RequestMapping("/api/players")
    public String getPlayers(HttpServletResponse response, @RequestParam @Parameter(hidden = true) Map<String, String> params) throws JsonProcessingException {
        response.setContentType("application/json");
        ApiKey apiKey = getApiKeyFromParams(params);
        if (apiKey == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> error = Map.of("error", "Invalid API key");
            return objectMapper.writeValueAsString(error);
        }

        List<Player> players = restApiService.getPlayers(params);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (Player player : players) {
            arrayNode.add(player.toJson());
        }
        root.put("players", arrayNode);
        restApiService.logAccess(apiKey, "/api/players", params);
        return root.toString();
    }


    @Operation(summary = "Create a new player account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "Example response",
                                    value = "{ \"success\" : true, \"id\": 1 }")}
                    )}),
            @ApiResponse(responseCode = "401", description = "Unauthorized if invalid api key",
                    content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response",
                            value = "{\"error\":\"Invalid API key\"}"
                    )})),
            @ApiResponse(responseCode = "400", description = "Could not add user due to validation errors",
                    content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response",
                            value = "{\"field1\":\"Error description in field 1\", \"field2\":\"Error description in field 2\"}"
                    )}))
    })
    @RequestMapping(value="/api/players", method=RequestMethod.POST)
    public String addPlayer(@RequestHeader(name = "API-KEY") String apiKeyStr,
                            HttpServletResponse response, @RequestBody PlayerDto playerDto) throws JsonProcessingException {
        response.setContentType("application/json");
        ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
        if (apiKey == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> error = Map.of("error", "Invalid API key");
            return objectMapper.writeValueAsString(error);
        }

        playerDto.setPasswordConfirm(playerDto.getPassword());

        Errors err = new SimpleErrors(playerDto);
        signUpValidator.validate(playerDto, err);

        if (err.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode errorNode = objectMapper.createObjectNode();
            for (ObjectError error : err.getAllErrors()) {
                ((ObjectNode) errorNode).put(((FieldError)error).getField(), error.getDefaultMessage());
            }

            return errorNode.toString();
        }

        Long id = playerService.addNewPlayer(playerDto).getId();

        restApiService.logAccess(apiKey, "/api/players", Map.of("apiKey", apiKey.getKeyToken(), "user", playerDto.toString()));
        return "{ \"success\" : true, \"id\": " + id + " }";
    }

    @Operation(summary = "Update a player account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "Example response",
                                    value = "{ \"success\" : true }")}
                    )}),
            @ApiResponse(responseCode = "401", description = "Unauthorized if invalid api key",
                    content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response",
                            value = "{\"error\":\"Invalid API key\"}"
                    )})),
            @ApiResponse(responseCode = "400", description = "Request body errors (check dropdown for more)",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "Missing data", value = "{\"error\":\"No data provided\"}"),
                            @ExampleObject(name = "Invalid data", value = "{\"error\":\"Missing data or null data\"}"),
                            @ExampleObject(name = "Validation errors", value = "{\"field1\":\"Error description in field 1\", \"field2\":\"Error description in field 2\"}")
                    })),
            @ApiResponse(responseCode = "404", description = "Player with the given ID not found",
                    content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response",
                            value = "{\"error\":\"Player not found\"}"
                    )}))
    })
    @PatchMapping("/api/players/{id}")
    public String updatePlayer(@RequestHeader(name = "API-KEY") String apiKeyStr,
                               HttpServletResponse response, @PathVariable Long id, @RequestBody PlayerDto playerDto) throws JsonProcessingException {
        response.setContentType("application/json");
        ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
        if (apiKey == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> error = Map.of("error", "Invalid API key");
            return objectMapper.writeValueAsString(error);
        }

        Optional<Player> player = playerService.getUser(id);
        if (player.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> error = Map.of("error", "Player not found");
            return objectMapper.writeValueAsString(error);
        }

        if (playerDto == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> error = Map.of("error", "No data provided");
            return objectMapper.writeValueAsString(error);
        }

        if (playerDto.getUsername() == null || playerDto.getRoles() == null || playerDto.getEmail() == null || playerDto.getPassword() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> error = Map.of("error", "Missing data or null data");
            return objectMapper.writeValueAsString(error);
        }

        playerDto.setPasswordConfirm(playerDto.getPassword());

        Errors err = new SimpleErrors(playerDto);
        signUpValidator.validate(playerDto, err);

        if (err.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode errorNode = objectMapper.createObjectNode();
            for (ObjectError error : err.getAllErrors()) {
                ((ObjectNode) errorNode).put(((FieldError)error).getField(), error.getDefaultMessage());
            }

            return errorNode.toString();
        }

        playerService.updatePlayer(id, playerDto);
        return "{ \"success\" : true }";
    }

    @Operation(summary = "Delete a player account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "Example response",
                                    value = "{ \"success\" : true }")}
                    )}),
            @ApiResponse(responseCode = "401", description = "Unauthorized if invalid api key",
                    content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response",
                            value = "{\"error\":\"Invalid API key\"}"
                    )})),
            @ApiResponse(responseCode = "404", description = "Player with the given ID not found",
                    content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response",
                            value = "{\"error\":\"Player not found\"}"
                    )}))
    })
    @DeleteMapping("/api/players/{id}")
    public String deletePlayer(@RequestHeader("API-KEY") String apiKeyStr,
                               HttpServletResponse response, @PathVariable Long id) throws JsonProcessingException {
        response.setContentType("application/json");
        ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
        if (apiKey == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> error = Map.of("error", "Invalid API key");
            return objectMapper.writeValueAsString(error);
        }

        Optional<Player> player = playerService.getUser(id);
        if (player.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> error = Map.of("error", "Player not found");
            return objectMapper.writeValueAsString(error);
        }

        playerService.deletePlayer(id);
        return "{ \"success\" : true }";
    }

    private ApiKey getApiKeyFromParams(Map<String, String> params) {
        if (!params.containsKey("apiKey")) {
            return null;
        }

        String apiKey = params.get("apiKey");
        return apiKeyService.getApiKey(apiKey);
    }
}
