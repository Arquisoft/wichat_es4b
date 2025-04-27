package com.uniovi.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.dto.PlayerDto;
import com.uniovi.entities.ApiKey;
import com.uniovi.entities.Player;
import com.uniovi.entities.Role;
import com.uniovi.services.ApiKeyService;
import com.uniovi.services.PlayerService;
import com.uniovi.services.RestApiService;
import com.uniovi.validators.SignUpValidator;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * Se define el controlador para la API de jugadores.
 *
 * En primera instancia se definen que métodos se van a definir y cuáles no, y el porqué de cada uno.
 *
 * POST /api/players - Crea un nuevo usuario.
 * 	 Acceso restringido solo a personas registradas.
 *
 * GET /api/players - Obtiene la lista de todos los usuarios.
 *   Acceso restringido solo a personas registradas.
 *
 * GET /api/players/{id} - Obtiene los detalles de un usuario específico.
 *   Acceso restringido solo a personas registradas.
 *
 * PUT /api/players/{id} - Actualiza completamente los datos de un usuario específico.
 *   Restringido al propio usuario a modificar o a administradores.
 *
 * PATCH /api/players/{id} - Actualiza parcialmente los datos de un usuario específico.
 *   Restringido al propio usuario a modificar o a administradores.
 *
 * DELETE /api/players/{id} - Elimina un usuario específico.
 *   Restringido al propio usuario a modificar o a administradores.
 *
 * Métodos que no se implementan:
 *
 * DELETE /api/players - No se permite eliminar todos los usuarios a la vez por seguridad y preservación del sistema.
 *
 * PUT /api/players - No se permite la actualización masiva de todos los usuarios simultáneamente.
 */

@OpenAPIDefinition(
		info = @Info(
				title = "WIChat Game API", version = "1.0",
				description = "API for managing players and questions\nTo get access, please generate an API key in the webpage"
		), servers = {@Server(
		url = "https://WIChat.es", description = "Production server"
), @Server(url = "http://localhost:3000", description = "Local server"),}
)
@Tag(
		name = "Player API", description = """
										   API for managing players. The following HTTP methods are defined with specific access restrictions and security considerations:
										   - POST /api/players: Creates a new player. Restricted to registered users only.
										   - GET /api/players: Retrieves the list of all players. Restricted to registered users only.
										   - GET /api/players/{id}: Retrieves the details of a specific player. Restricted to registered users only.
										   - PUT /api/players/{id}: Fully updates the data of a specific player. Restricted to the player themselves or administrators.
										   - PATCH /api/players/{id}: Partially updates the data of a specific player. Restricted to the player themselves or administrators.
										   - DELETE /api/players/{id}: Deletes a specific player. Restricted to the player themselves or administrators.
										   
										   Methods that are not implemented due to security and system integrity reasons:
										   - DELETE /api/players: Bulk deletion of all players is not allowed to ensure the integrity of the system.
										   - PUT /api/players: Bulk update of all players is not allowed to prevent system-wide inconsistencies and data integrity issues.
										   """
)

@RestController
public class PlayerApiController {

	public static final String HTTP_URL_API = "/api";
	public static final String HTTP_URL_API_PLAYERS = HTTP_URL_API + "/players";
	public static final String API_KEY_HEADER = "API-KEY";
	public static final String RESP_SUCCESS = "{ \"success\" : true }";
	public static final String RESP_INVALID_API_KEY = "{\"error\":\"Invalid API key\"}";
	public static final String RESP_TYPE = "application/json";
	public static final String DESC_ERR = "Error response";
	public static final String DESC_EXP = "Example response";
	public static final String DESC_PLAYER_NOT_FOUND = "Player not found";
	public static final String DESC_INVALID_KEY = "Unauthorized if invalid api key";

	private final ApiKeyService apiKeyService;
	private final RestApiService restApiService;
	private final PlayerService playerService;
	private final SignUpValidator signUpValidator;

	private final ApiUtils apiUtils;

	public PlayerApiController(ApiKeyService apiKeyService, RestApiService restApiService,
							   SignUpValidator signUpValidator,
							   PlayerService playerService) {
		this.apiKeyService   = apiKeyService;
		this.restApiService  = restApiService;
		this.playerService   = playerService;
		this.signUpValidator = signUpValidator;

		this.apiUtils = new ApiUtils();
	}


	// Crear nuevo jugador
	@PostMapping(value = HTTP_URL_API_PLAYERS)
	@Operation(
			summary = "Create a new player account.",
			description = "Creates a new player account with the provided player information. Access is restricted to registered users with a valid API key."
	)
	@ApiResponses(
			value = {@ApiResponse(
					responseCode = "200", description = "Success", content = {@Content(
					mediaType = RESP_TYPE, examples = {@ExampleObject(
					name = DESC_EXP, value = "{ \"success\" : true, \"id\": 1 }"
			)}
			)}
			), @ApiResponse(
					responseCode = "401", description = DESC_INVALID_KEY,
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = RESP_INVALID_API_KEY
					)}
					)
			), @ApiResponse(
					responseCode = "400",
					description = "Could not add user due to validation errors",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR,
							value = "{\"field1\":\"Error description in field 1\", \"field2\":\"Error description in field 2\"}"
					)}
					)
			)}
	)
	public String createPlayer(@RequestHeader(API_KEY_HEADER) String apiKeyStr,
							   @RequestBody PlayerDto playerDto,
							   HttpServletResponse response)
			throws JsonProcessingException {
		//Tipo de respuesta
		response.setContentType(RESP_TYPE);

		//Obtención del usuario de la petición en función del API-KEY
		ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
		if (apiKey == null) return apiUtils.responseForAPiKeyNull(response);

		//Comprobar que el usuario a crear es válido
		String error = apiUtils.checkErrorForPassword(signUpValidator, response,
													  playerDto);
		if (error != null) return error;

		// Añado al nuevo usuario y obtengo su nuevo ID
		Long id = playerService.addNewPlayer(playerDto).getId();

		// Registro de acceso a la API
		restApiService.logAccess(apiKey, HTTP_URL_API_PLAYERS,
								 Map.of("apiKey", apiKey.getKeyToken(), "http-method",
										"post", "user", playerDto.toString()));

		//Respuesta satisfactoria
		response.setStatus(HttpServletResponse.SC_OK);
		return new ObjectMapper().writeValueAsString(Map.of("success", true, "id", id));
	}


	// Obtener jugadores filtrados
	@GetMapping(HTTP_URL_API_PLAYERS)
	@Operation(
			summary = "Retrieve player accounts information with optional filters.",
			description = "Retrieves a list of player accounts, optionally filtered by query parameters parameters such as username, email, id or roles. Access is restricted to registered users with a valid API key."
	)
	@Parameters(
			{@Parameter(
					name = "apiKey", description = "API key for authentication",
					required = true
			), @Parameter(name = "id", description = "ID of the player"), @Parameter(
					name = "username", description = "Username of the player"
			), @Parameter(
					name = "email", description = "Email of the player"
			), @Parameter(
					name = "role",
					description = "Role of the player. Will return players that have this role."
			),}
	)
	@ApiResponses(
			value = {@ApiResponse(
					responseCode = "200", description = "Success", content = {@Content(
					mediaType = RESP_TYPE, examples = {@ExampleObject(
					name = DESC_EXP, value = """
											 {
											    "players":[
											       {
											          "id":1,
											          "username":"student1",
											          "email":"student1@example.com",
											          "roles":[
											             "ROLE_USER"
											          ],
											          "gameSessions":[
											             {
											                "id":1,
											                "player":1,
											                "correctQuestions":10,
											                "totalQuestions":40,
											                "createdAt":"2024-03-04T22:44:41.067901",
											                "finishTime":"2024-03-04T22:49:41.067901",
											                "score":0
											             }
											          ]
											       }
											    ]
											 }"""
			)}
			)}
			), @ApiResponse(
					responseCode = "401", description = DESC_INVALID_KEY,
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = RESP_INVALID_API_KEY
					)}
					)
			)}
	)
	public String listPlayers(
			@RequestParam @Parameter(hidden = true) Map<String, String> params,
			HttpServletResponse response) throws JsonProcessingException {
		// Tipo de respuesta
		response.setContentType(RESP_TYPE);

		// Obtención del usuario de la petición en función del API-KEY
		ApiKey apiKey = apiUtils.getApiKeyFromParams(apiKeyService, params);
		if (apiKey == null) return apiUtils.responseForAPiKeyNull(response);

		// Obtención de los jugadores y filtrado
		List<Player> players = restApiService.getPlayers(params);

		ObjectMapper objectMapper = new ObjectMapper();

		ArrayNode arrayNode = objectMapper.createArrayNode();
		for (Player player : players) {
			arrayNode.add(player.toJson());
		}

		ObjectNode root = objectMapper.createObjectNode(); // Crear el nodo raíz de la respuesta
		root.put("players", arrayNode);

		// Registro de acceso a la API
		restApiService.logAccess(apiKey, HTTP_URL_API_PLAYERS,
								 Map.of("apiKey", apiKey.getKeyToken(), "http-method",
										"get", "params", params.toString()));

		// Respuesta satisfactoria
		response.setStatus(HttpServletResponse.SC_OK);
		return root.toString();
	}

	// Obtener un jugador por ID
	@GetMapping(HTTP_URL_API_PLAYERS + "/{id}")
	@Operation(
			summary = "Retrieve a specific player account information by ID.",
			description = "Retrieves the details of a specific player account by ID. Access is restricted to registered users with a valid API key."
	)
	public String getPlayerById(@RequestHeader(API_KEY_HEADER) String apiKeyStr,
								@PathVariable Long id, HttpServletResponse response)
			throws JsonProcessingException {
		// Preparamos los parámetros como si vinieran en la petición
		Map<String, String> params = new HashMap<>();
		params.put("apiKey", apiKeyStr);
		params.put("id", id.toString());

		// Llamamos al metodo general pasándole los nuevos params
		return listPlayers(params, response);
	}

	// Actualizar parcialmente un jugador (PATCH)
	@PatchMapping(HTTP_URL_API_PLAYERS + "/{id}")
	@Operation(
			summary = "Partially update a player's data.",
			description = "Applies partial updates to a specific player account. Access is restricted to the account owner or administrators."
	)
	@ApiResponses(
			value = {@ApiResponse(
					responseCode = "200", description = "Success", content = {@Content(
					mediaType = RESP_TYPE, examples = {@ExampleObject(
					name = DESC_EXP, value = RESP_SUCCESS
			)}
			)}
			), @ApiResponse(
					responseCode = "401", description = DESC_INVALID_KEY,
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = RESP_INVALID_API_KEY
					)}
					)
			), @ApiResponse(
					responseCode = "400",
					description = "Request body errors (check dropdown for more)",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = "Missing data",
							value = "{\"error\":\"No data provided\"}"
					), @ExampleObject(
							name = "Validation errors",
							value = "{\"field1\":\"Error description in field 1\", \"field2\":\"Error description in field 2\"}"
					)}
					)
			), @ApiResponse(
					responseCode = "404",
					description = "Player with the given ID not found",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = "{\"error\":\"Player not found\"}"
					)}
					)
			)


			}
	)
	public String partialUpdatePlayer(@RequestHeader(API_KEY_HEADER) String apiKeyStr,
									  @PathVariable Long id,
									  @RequestBody PlayerDto playerDto,
									  HttpServletResponse response)
			throws JsonProcessingException {

		//Comprobar que el map exista
		if (playerDto == null)
			return apiUtils.resToError(response, HttpServletResponse.SC_BAD_REQUEST,
									   "No data provided");

		//Obtengo el usuario al que hacen referencia
		Optional<Player> player = playerService.getUser(id);
		Player playerToUpdate;

		//Comprobar que el usuario a modificar existe
		if (player.isEmpty())
			return apiUtils.resToError(response, HttpServletResponse.SC_NOT_FOUND,
									   DESC_PLAYER_NOT_FOUND);
		else playerToUpdate = player.get();

		PlayerDto updatedplayerDto = new PlayerDto();
		updatedplayerDto.setRoles(playerToUpdate.getRoles().stream().map(Role::getName)
										  .toArray(String[]::new));

		if (playerDto.getUsername() != null) {
			Errors err = new SimpleErrors(playerDto);
			signUpValidator.validateUsername(playerDto, err);
			if (err.hasErrors())
				return apiUtils.resToError(response, HttpServletResponse.SC_BAD_REQUEST,
										   "Invalid username data provided. " +
												   err.getFieldError("username"));
			updatedplayerDto.setUsername(playerDto.getUsername());
		} else updatedplayerDto.setUsername(playerToUpdate.getUsername());


		if (playerDto.getEmail() != null) {
			Errors err = new SimpleErrors(playerDto);
			signUpValidator.validateEmail(playerDto, err);
			if (err.hasErrors())
				return apiUtils.resToError(response, HttpServletResponse.SC_BAD_REQUEST,
										   "Invalid email data provided" +
												   err.getFieldError("email"));
			updatedplayerDto.setEmail(playerDto.getEmail());
		} else updatedplayerDto.setUsername(playerToUpdate.getEmail());

		if (playerDto.getPassword() != null) {
			String error = apiUtils.checkErrorForPassword(signUpValidator, response,
														  playerDto);
			if (error != null) return error;
			updatedplayerDto.setPassword(playerDto.getPassword());
		} else updatedplayerDto.setPassword(playerToUpdate.getPassword());


		// Llamamos a la lógica de actualización completa con la lógica ya aplicada
		return fullUpdatePlayer(apiKeyStr, id, updatedplayerDto, response);

	}

	// Actualizar completamente un jugador (PUT)
	@PutMapping(HTTP_URL_API_PLAYERS + "/{id}")
	@Operation(
			summary = "Fully update a player's data.",
			description = "Replaces all information of a specific player account. Access is restricted to the account owner or administrators."
	)
	@ApiResponses(
			value = {@ApiResponse(
					responseCode = "200", description = "Success", content = {@Content(
					mediaType = RESP_TYPE, examples = {@ExampleObject(
					name = DESC_EXP, value = RESP_SUCCESS
			)}
			)}
			), @ApiResponse(
					responseCode = "401", description = DESC_INVALID_KEY,
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = RESP_INVALID_API_KEY
					)}
					)
			), @ApiResponse(
					responseCode = "400",
					description = "Request body errors (check dropdown for more)",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = "Missing data",
							value = "{\"error\":\"No data provided\"}"
					), @ExampleObject(
							name = "Invalid data",
							value = "{\"error\":\"Missing data or null data\"}"
					), @ExampleObject(
							name = "Validation errors",
							value = "{\"field1\":\"Error description in field 1\", \"field2\":\"Error description in field 2\"}"
					)}
					)
			), @ApiResponse(
					responseCode = "404",
					description = "Player with the given ID not found",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = "{\"error\":\"Player not found\"}"
					)}
					)
			)}
	)
	public String fullUpdatePlayer(@RequestHeader(API_KEY_HEADER) String apiKeyStr,
								   @PathVariable Long id,
								   @RequestBody PlayerDto playerDto,
								   HttpServletResponse response)
			throws JsonProcessingException {
		//Tipo de respuesta
		response.setContentType(RESP_TYPE);

		//Obtención del usuario de la petición en función del API-KEY
		ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
		if (apiKey == null) return apiUtils.responseForAPiKeyNull(response);

		//Obtengo el usuario al que hacen referencia
		Optional<Player> player = playerService.getUser(id);

		//Comprobar que el usuario a modificar existe
		if (player.isEmpty())
			return apiUtils.resToError(response, HttpServletResponse.SC_NOT_FOUND,
									   DESC_PLAYER_NOT_FOUND);

		//Comprobar que los datos del dto existan
		if (playerDto == null)
			return apiUtils.resToError(response, HttpServletResponse.SC_BAD_REQUEST,
									   "No data provided");


		//Comprobar que el usuario tiene permisos para actualizar al jugador
		if (apiUtils.isAdminOrOwn(apiKey, id)) {

			//Se comprueba que los datos internos del dto no sean nulos
			if (playerDto.getUsername() == null || playerDto.getRoles() == null ||
					playerDto.getEmail() == null || playerDto.getPassword() == null) {
				return apiUtils.resToError(response, HttpServletResponse.SC_BAD_REQUEST,
										   "Missing data or null data");
			}

			String error = apiUtils.checkErrorForPassword(signUpValidator, response,
														  playerDto);
			if (error != null) return error;

			//Se actualiza el jugador con los datos del DTO
			playerService.updatePlayer(id, playerDto);

			// Registro de acceso a la API
			restApiService.logAccess(apiKey, HTTP_URL_API_PLAYERS,
									 Map.of("apiKey", apiKey.getKeyToken(), "http-method",
											"put", "id", id.toString(), "user",
											playerDto.toString()));

			//Respuesta satisfactoria
			response.setStatus(HttpServletResponse.SC_OK);
			return new ObjectMapper().writeValueAsString(
					Map.of("success", true, "id", id));
		}

		//Respuesta de error - No autorizado
		return apiUtils.resToError(response, HttpServletResponse.SC_UNAUTHORIZED,
								   "Unauthorized action");
	}

	// Eliminar un jugador
	@DeleteMapping(HTTP_URL_API_PLAYERS + "/{id}")
	@Operation(
			summary = "Delete a player account by ID.",
			description = "Deletes a specific player account identified by its ID. Access is restricted to the account owner or administrators."
	)
	@ApiResponses(
			value = {@ApiResponse(
					responseCode = "200", description = "Success", content = @Content(
					mediaType = RESP_TYPE, examples = {@ExampleObject(
					name = DESC_EXP, value = RESP_SUCCESS
			)}
			)
			), @ApiResponse(
					responseCode = "401",
					description = "Invalid or Unauthorized api key ", content = @Content(
					mediaType = RESP_TYPE, examples = {@ExampleObject(
					name = DESC_ERR, value = RESP_INVALID_API_KEY
			), @ExampleObject(name = DESC_ERR, value = "Unauthorized action")}
			)
			), @ApiResponse(
					responseCode = "404",
					description = "Player with the given ID not found",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = "{\"error\":\"Player not found\"}"
					)}
					)
			)}
	)

	public String deletePlayer(@RequestHeader(API_KEY_HEADER) String apiKeyStr,
							   @PathVariable Long id, HttpServletResponse response)
			throws JsonProcessingException {
		//Tipo de respuesta
		response.setContentType(RESP_TYPE);

		//Obtención del usuario de la petición en función del API-KEY
		ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
		if (apiKey == null) return apiUtils.responseForAPiKeyNull(response);

		//Obtención del usuario sobre el que se realiza la acción
		Optional<Player> player = playerService.getUser(id);
		if (player.isEmpty())
			return apiUtils.resToError(response, HttpServletResponse.SC_NOT_FOUND,
									   DESC_PLAYER_NOT_FOUND);

		//Comprobar que el usuario tiene permisos para eliminar el jugador
		if (apiUtils.isAdminOrOwn(apiKey, id)) {
			//Eliminación del jugador
			playerService.deletePlayer(id);

			//Respuesta satisfactoria
			response.setStatus(HttpServletResponse.SC_OK);
			return new ObjectMapper().writeValueAsString(Map.of("success", true));
		}

		//Respuesta de error - No autorizado
		return apiUtils.resToError(response, HttpServletResponse.SC_UNAUTHORIZED,
								   "Unauthorized action");
	}
}
