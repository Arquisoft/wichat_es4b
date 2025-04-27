package com.uniovi.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.ApiKey;
import com.uniovi.entities.QuestionImage;
import com.uniovi.services.ApiKeyService;
import com.uniovi.services.RestApiService;
import com.uniovi.services.impl.QuestionImageServiceImpl;
import com.uniovi.validators.QuestionValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.uniovi.controllers.api.PlayerApiController.*;

/*
 * Métodos HTTP para la gestión de preguntas en la API REST:
 *
 * GET /preguntas - Obtiene la lista de todas las preguntas.
 *   Disponible para cualquier usuario con una clave API válida.
 *
 * POST /preguntas - Crea una nueva pregunta.
 *   Restringido solo a administradores.
 *
 * PATCH /preguntas/{id} - Actualiza parcialmente una pregunta específica.
 *   Restringido solo a administradores.
 *
 * DELETE /preguntas/{id} - Elimina una pregunta específica.
 *   Restringido solo a administradores.
 *
 * Métodos que no se implementan:
 *
 * PUT /preguntas/{id} - Actualiza completamente una pregunta específica. No se permite por motivos de seguridad
 *
 * DELETE /preguntas - No se permite eliminar todas las preguntas a la vez por seguridad e integridad del sistema.
 *
 * PUT /preguntas - No se permite la actualización masiva de todas las preguntas simultáneamente.
 */

@Tag(
		name = "Questions API for Image Game", description = """
															 API for managing questions. Some HTTP methods are restricted for security and system integrity reasons:
															 - GET /api/questions: Retrieves the list of all questions. Available to any user with a valid API key.
															 - POST /api/questions: Creates a new question. Restricted to administrators only.
															 - PATCH /api/questions/{id}: Partially updates a specific question. Restricted to administrators only.
															 - DELETE /api/questions/{id}: Deletes a specific question. Restricted to administrators only.
															 Methods not implemented due to security reasons:
															 - PUT /api/questions/{id}: Fully updates a specific question. Not allowed for security reasons.
															 - DELETE /api/questions: Bulk delete of all questions is not allowed to ensure the integrity of the system.
															 - PUT /api/questions: Bulk update of all questions is not allowed to prevent system-wide inconsistencies."""
)
@RestController
public class QuestionImageGameApiController {

	public static final String HTTP_URL_API_IMAGE_QUESTION = HTTP_URL_API + "/questions";

	private final ApiKeyService apiKeyService;
	private final RestApiService apiService;
	private final QuestionImageServiceImpl questionService;
	private final QuestionValidator questionValidator;

	private final ApiUtils apiUtils;

	public QuestionImageGameApiController(ApiKeyService apiKeyService,
										  RestApiService apiService,
										  QuestionImageServiceImpl questionService,
										  QuestionValidator questionValidator) {
		this.apiKeyService     = apiKeyService;
		this.apiService        = apiService;
		this.questionService   = questionService;
		this.questionValidator = questionValidator;

		this.apiUtils = new ApiUtils();
	}

	/**
	 * Obtiene la lista de todas las preguntas.
	 * Disponible para cualquier usuario con una clave API válida.
	 */
	@GetMapping(HTTP_URL_API_IMAGE_QUESTION)
	@Operation(
			summary = "Fetch questions, with different params available for management",
			description =
					"Fetch questions based on the provided parameters such as category, statement, or id. " +
							"The results are paginated, and pagination can be controlled using the 'page' and 'size' parameters. " +
							"Access is restricted to registered users with a valid API key."
	)
	@Parameters(
			{@Parameter(
					name = "apiKey", description = "API key for authentication",
					required = true
			), @Parameter(
					name = "category",
					description = "Category of the question. Case sensitive"
			), @Parameter(
					name = "statement",
					description = "Text contained in the statement of the question"
			), @Parameter(
					name = "id", description = "ID of the question"
			)}
	)
	@ApiResponses(
			value = {@ApiResponse(
					responseCode = "200", description = "Success", content = {@Content(
					mediaType = RESP_TYPE, examples = {@ExampleObject(
					name = DESC_EXP, value = """
											 {
											    "questions":[
											       {
											          "id":11802,
											          "statement":"Which countries share a border with Solomon Islands?",
											          "category":{
											             "id":1,
											             "name":"Geography",
											             "description":"Questions about geography"
											          },
											          "options":[
											             {
											                "id":46252,
											                "text":"Papua New Guinea",
											                "correct":true,
											                "question":11802
											             },
											             {
											                "id":46253,
											                "text":"Venezuela",
											                "correct":false,
											                "question":11802
											             },
											             {
											                "id":46254,
											                "text":"Austria",
											                "correct":false,
											                "question":11802
											             },
											             {
											                "id":46255,
											                "text":"United States of America",
											                "correct":false,
											                "question":11802
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
	public String listQuestions(
			@RequestParam @Parameter(hidden = true) Map<String, String> params,
			@ParameterObject Pageable pageable, HttpServletResponse response)
			throws JsonProcessingException {
		//Tipo de respuesta
		response.setContentType(RESP_TYPE);

		//Obtención del usuario de la petición en función del API-KEY
		ApiKey apiKey = apiUtils.getApiKeyFromParams(apiKeyService, params);
		if (apiKey == null) return apiUtils.responseForAPiKeyNull(response);

		// Obtención y verificación de las preguntas sobre la que se realiza la acción

		ObjectMapper objectMapper = new ObjectMapper();
		ArrayNode arrayNode = objectMapper.createArrayNode();


		for (QuestionImage question : apiService.getQuestionsImageGame(params, pageable))
			arrayNode.add(question.toJson());

		// Registro de acceso a la API
		apiService.logAccess(apiKey, HTTP_URL_API_IMAGE_QUESTION,
							 Map.of("apiKey", apiKey.getKeyToken(), "http-method", "get",
									"params", params.toString()));

		//Respuesta satisfactoria
		response.setStatus(HttpServletResponse.SC_OK);
		return objectMapper.createObjectNode().set("questions", arrayNode).toString();
	}

	// Obtener un jugador por ID
	@GetMapping(HTTP_URL_API_IMAGE_QUESTION + "/{id}")
	@Operation(
			summary = "Retrieve a specific questions information by ID.",
			description = "Retrieves the details of a specific questions by ID. Access is restricted to registered users with a valid API key."
	)
	public String getQuestionById(@RequestHeader(API_KEY_HEADER) String apiKeyStr,
								  @PathVariable Long id, HttpServletResponse response)
			throws JsonProcessingException {
		// Preparamos los parámetros como si vinieran en la petición
		Map<String, String> params = new HashMap<>();
		params.put("apiKey", apiKeyStr);
		params.put("id", id.toString());

		// Crear un Pageable con un solo elemento (sin paginación real)
		Pageable pageable = PageRequest.of(0, 1); // Página 0 con un solo elemento

		// Llamamos al metodo general pasándole los nuevos params
		return listQuestions(params, pageable, response);
	}

	/**
	 * Crea una nueva pregunta.
	 * Restringido solo a administradores.
	 */
	@PostMapping(HTTP_URL_API_IMAGE_QUESTION)
	@Operation(
			summary = "Add a new question", description = """
														  Add a new question to the database. \
														  The question must include:
														  - A non-empty statement.
														  - A category (must exist in the database).
														  - Exactly four options.
														  - An optional imageUrl for the question.
														  - A language field (e.g., 'en', 'es').
														  The correct answer must be properly marked in the options list. \
														  Access is restricted to administrators with a valid API key."""
	)
	@ApiResponses(
			value = {@ApiResponse(
					responseCode = "200", description = "Success", content = {@Content(
					mediaType = RESP_TYPE, examples = {@ExampleObject(
					name = DESC_EXP, value = "{\"success\": true, \"id\": 1}"
			)}
			)}
			), @ApiResponse(
					responseCode = "400",
					description = "Bad request if the data is missing or invalid",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = "{\"error\":\"Missing data\"}"
					), @ExampleObject(
							name = "Validation errors",
							value = "{\"field1\":\"Error description in field 1\", \"field2\":\"Error description in field 2\"}"
					)}
					)
			)}
	)
	public String createQuestion(@RequestHeader(API_KEY_HEADER) String apiKeyStr,
								 @RequestBody QuestionImageDto question,
								 HttpServletResponse response)
			throws JsonProcessingException {
		//Tipo de respuesta
		response.setContentType(RESP_TYPE);

		//Comprobación de que se incluye el API-KEY válido en la petición
		ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
		if (apiKey == null) return apiUtils.responseForAPiKeyNull(response);

		//Comprobación de que se incluyen los datos para la creación de la pregunta
		if (question == null)
			return apiUtils.resToError(response, HttpServletResponse.SC_BAD_REQUEST,
									   "Missing data");

		//Comprobar que el usuario tiene permisos para crear la pregunta
		if (apiUtils.isAdmin(apiKey)) {

			//Comprobación de que se incluyen datos correctos para la creación de la pregunta
			String error = apiUtils.checkErrorForImageQuestion(questionValidator,
															   question, response);
			if (error != null) return error;

			//Incluimos la pregunta en la base de datos
			Long id = questionService.addNewQuestion(question).getId();

			// Registro de acceso a la API
			apiService.logAccess(apiKey, HTTP_URL_API_IMAGE_QUESTION,
								 Map.of("apiKey", apiKey.getKeyToken(), "http-method",
										"post", "question", question.toString()));

			//Respuesta satisfactoria
			response.setStatus(HttpServletResponse.SC_OK);
			return new ObjectMapper().writeValueAsString(
					Map.of("success", true, "id", id));
		}

		//Respuesta de error - No autorizado
		return apiUtils.resToError(response, HttpServletResponse.SC_UNAUTHORIZED,
								   "Unauthorized action");
	}

	/**
	 * Actualiza parcialmente una pregunta específica.
	 * Restringido solo a administradores.
	 */
	@PatchMapping(HTTP_URL_API_IMAGE_QUESTION + "/{id}")
	@Operation(
			summary = "Update a question",
			description = "Update a question in the database. The question could have a statement, a category, 4 answer options and/or a url. The correct option must be marked as such."
	)
	@ApiResponses(
			value = {@ApiResponse(
					responseCode = "200", description = "Success", content = {@Content(
					mediaType = RESP_TYPE, examples = {@ExampleObject(
					name = DESC_EXP, value = "{\"success\": true}"
			)}
			)}
			), @ApiResponse(
					responseCode = "400",
					description = "Bad request if the data is missing or invalid",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = "{\"error\":\"Missing data\"}"
					), @ExampleObject(
							name = "Validation errors",
							value = "{\"field1\":\"Error description in field 1\", \"field2\":\"Error description in field 2\"}"
					)}
					)
			), @ApiResponse(
					responseCode = "404",
					description = "Not found if the question does not exist",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = "{\"error\":\"Question not found\"}"
					)}
					)
			), @ApiResponse(
					responseCode = "401",
					description = "Unauthorized if the user does not have permission to modify the question",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = "Unauthorized",
							value = "{\"error\":\"Unauthorized action\"}"
					)}
					)
			)}
	)
	public String partialUpdateQuestion(@RequestHeader(API_KEY_HEADER) String apiKeyStr,
										@PathVariable Long id,
										@RequestBody QuestionImageDto question,
										HttpServletResponse response)
			throws JsonProcessingException {
		//Tipo de respuesta
		response.setContentType(RESP_TYPE);

		//Comprobación de que se incluye el API-KEY válido en la petición
		ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
		if (apiKey == null) return apiUtils.responseForAPiKeyNull(response);

		//Comprobación de que se incluyen los datos para la actualización de la pregunta
		if (question == null)
			return apiUtils.resToError(response, HttpServletResponse.SC_BAD_REQUEST,
									   "Missing data");

		//Comprobar que el usuario tiene permisos para crear la pregunta
		if (apiUtils.isAdmin(apiKey)) {

			//Comprobación de que exista la pregunta a actualizar
			QuestionImage q = questionService.getQuestion(id).orElse(null);
			if (q == null)
				return apiUtils.resToError(response, HttpServletResponse.SC_NOT_FOUND,
										   "Question not found");


			//Comprobación de que se incluyen datos correctos para la actualización de la pregunta
			String error = apiUtils.validateAndSetCorrectAnswer(question,
																questionValidator,
																response);
			if (error != null) return error;

			//Modificamos la pregunta
			questionService.updateQuestion(id, question);

			//Respuesta satisfactoria
			response.setStatus(HttpServletResponse.SC_OK);
			return new ObjectMapper().writeValueAsString(
					Map.of("success", true, "id", id));
		}

		//Respuesta de error - No autorizado
		return apiUtils.resToError(response, HttpServletResponse.SC_UNAUTHORIZED,
								   "Unauthorized action");
	}

	/**
	 * Elimina una pregunta específica.
	 * Restringido solo a administradores.
	 */
	@DeleteMapping(HTTP_URL_API_IMAGE_QUESTION + "/{id}")
	@Operation(
			summary = "Delete a question", description =
			"Deletes a question from the database. Restricted to administrators. " +
					"Requires a valid API key."
	)
	@ApiResponses(
			value = {@ApiResponse(
					responseCode = "200", description = "Success delete",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_EXP, value = RESP_SUCCESS
					)}
					)
			), @ApiResponse(
					responseCode = "401", description = DESC_INVALID_KEY,
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = "Invalid or Unauthorized api key"
					), @ExampleObject(
							name = DESC_ERR, value = "Unauthorized action"
					)}
					)
			), @ApiResponse(
					responseCode = "404",
					description = "Not found if the question does not exist",
					content = @Content(
							mediaType = RESP_TYPE, examples = {@ExampleObject(
							name = DESC_ERR, value = "{\"error\":\"Question not found\"}"
					)}
					)
			)}
	)
	public String eliminarPregunta(@RequestHeader(API_KEY_HEADER) String apiKeyStr,
								   @PathVariable Long id, HttpServletResponse response)
			throws JsonProcessingException {
		//Tipo de respuesta
		response.setContentType(RESP_TYPE);

		//Obtención del usuario de la petición en función del API-KEY
		ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
		if (apiKey == null) return apiUtils.responseForAPiKeyNull(response);

		//Comprobar que el usuario tiene permisos para eliminar la pregunta
		if (apiUtils.isAdmin(apiKey)) {
			// Obtención y verificación de la pregunta sobre la que se realiza la acción
			if (questionService.getQuestion(id).isEmpty())
				return apiUtils.resToError(response, HttpServletResponse.SC_NOT_FOUND,
										   "Question not found");

			//Eliminación de la pregunta
			questionService.deleteQuestion(id);

			// Registro de acceso a la API
			apiService.logAccess(apiKey, HTTP_URL_API_IMAGE_QUESTION + "/" + id,
								 Map.of("apiKey", apiKey.getKeyToken(), "http-method",
										"delete", "id", id.toString()));

			//Respuesta satisfactoria
			response.setStatus(HttpServletResponse.SC_OK);
			return new ObjectMapper().writeValueAsString(Map.of("success", true));
		}

		//Respuesta de error - No autorizado
		return apiUtils.resToError(response, HttpServletResponse.SC_UNAUTHORIZED,
								   "Unauthorized action");
	}
}
