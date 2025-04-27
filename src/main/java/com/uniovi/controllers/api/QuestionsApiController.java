package com.uniovi.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.ApiKey;
import com.uniovi.entities.Question;
import com.uniovi.entities.abstracts.AbstractQuestion;
import com.uniovi.services.ApiKeyService;
import com.uniovi.services.RestApiService;
import com.uniovi.services.impl.QuestionServiceImpl;
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
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Questions API", description = "API for managing questions")
@RestController
public class QuestionsApiController {

	private final ApiKeyService apiKeyService;
	private final RestApiService restApiService;
	private final QuestionServiceImpl questionService;
	private final QuestionValidator questionValidator;

	private final ApiUtils apiUtils;

	public QuestionsApiController(ApiKeyService apiKeyService, RestApiService restApiService,
								  QuestionServiceImpl questionService, QuestionValidator questionValidator) {
		this.apiKeyService = apiKeyService;
		this.restApiService = restApiService;
		this.questionService = questionService;
		this.questionValidator = questionValidator;

		this.apiUtils = new ApiUtils();
	}

	@Operation(summary = "Fetch questions, with different params available for management", description = "Fetch questions based on the provided parameters such as category, statement, id. The results are paged, and the page can be controlled with the page and size parameters.")
	@Parameters({@Parameter(name = "apiKey", description = "API key for authentication", required = true), @Parameter(name = "category", description = "Category of the question. Case sensitive"), @Parameter(name = "statement", description = "Text contained in the statement of the question"), @Parameter(name = "id", description = "ID of the question")})
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json", examples = {@ExampleObject(name = "Example response", value = "{\n" + "   \"questions\":[\n" + "      {\n" + "         \"id\":11802,\n" + "         \"statement\":\"Which countries share a border with Solomon Islands?\",\n" + "         \"category\":{\n" + "            \"id\":1,\n" + "            \"name\":\"Geography\",\n" + "            \"description\":\"Questions about geography\"\n" + "         },\n" + "         \"options\":[\n" + "            {\n" + "               \"id\":46252,\n" + "               \"text\":\"Papua New Guinea\",\n" + "               \"correct\":true,\n" + "               \"question\":11802\n" + "            },\n" + "            {\n" + "               \"id\":46253,\n" + "               \"text\":\"Venezuela\",\n" + "               \"correct\":false,\n" + "               \"question\":11802\n" + "            },\n" + "            {\n" + "               \"id\":46254,\n" + "               \"text\":\"Austria\",\n" + "               \"correct\":false,\n" + "               \"question\":11802\n" + "            },\n" + "            {\n" + "               \"id\":46255,\n" + "               \"text\":\"United States of America\",\n" + "               \"correct\":false,\n" + "               \"question\":11802\n" + "            }\n" + "         ]\n" + "      }\n" + "   ]\n" + "}")})}), @ApiResponse(responseCode = "401", description = "Unauthorized if invalid api key", content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response", value = "{\"error\":\"Invalid API key\"}")}))})
	@RequestMapping("/api/questions")
	public String getQuestions(@ParameterObject Pageable pageable, HttpServletResponse response,
							   @RequestParam @Parameter(hidden = true) Map<String, String> params) throws JsonProcessingException {

		response.setContentType("application/json");
		ApiKey apiKey = apiUtils.getApiKeyFromParams(apiKeyService, params);
		if (apiKey == null) apiUtils.responseForAPiKeyNull(response);

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode root = objectMapper.createObjectNode();
		ArrayNode arrayNode = objectMapper.createArrayNode();
		List<Question> questions = restApiService.getQuestions(params, pageable);
		for (Question question : questions) {
			arrayNode.add(question.toJson());
		}
		root.set("questions", arrayNode);
		restApiService.logAccess(apiKey, "/api/questions", params);
		return root.toString();
	}

	@Operation(summary = "Add a new question", description = "Add a new question to the database. The question must have a statement, a category, and 4 options. The correct option must be marked as such.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json", examples = {@ExampleObject(name = "Example response", value = "{\"success\": true, \"id\": 1}")})}), @ApiResponse(responseCode = "400", description = "Bad request if the data is missing or invalid", content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response", value = "{\"error\":\"Missing data\"}"), @ExampleObject(name = "Validation errors", value = "{\"field1\":\"Error description in field 1\", \"field2\":\"Error description in field 2\"}")}))})
	@RequestMapping(value = "/api/questions", method = RequestMethod.POST)
	public String addQuestion(HttpServletResponse response, @RequestHeader("API-KEY") String apiKeyStr,
							  @RequestBody QuestionDto questionDto) throws JsonProcessingException {

		ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
		if (apiKeyService.getApiKey(apiKeyStr) == null) apiUtils.responseForAPiKeyNull(response);

		if (questionDto == null)
			return apiUtils.responseToError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing data");
		if (questionDto.getStatement() == null || questionDto.getStatement().isEmpty())
			return apiUtils.responseToError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing data: question statement");
		if (questionDto.getLanguage() == null || questionDto.getLanguage().isEmpty())
			return apiUtils.responseToError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing data: question language");
		if (questionDto.getCategory() == null)
			return apiUtils.responseToError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing data: question category");
		if (questionDto.getOptions() == null || questionDto.getOptions().isEmpty())
			return apiUtils.responseToError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing data: question options");

		String error = apiUtils.checkErrorForQuestion(questionValidator, response, questionDto);

		if (error != null)
			return error;

		AbstractQuestion<?> q = questionService.addNewQuestion(questionDto);

		restApiService.logAccess(apiKey, "/api/questions", Map.of("apiKey", apiKeyStr, "question", questionDto.toString()));
		return "{ \"success\": true, \"id\":" + q.getId() + " }";
	}

	@Operation(summary = "Update a question", description = "Update a question in the database. The question must have a statement, a category, and 4 options. The correct option must be marked as such.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json", examples = {@ExampleObject(name = "Example response", value = "{\"success\": true}")})}), @ApiResponse(responseCode = "400", description = "Bad request if the data is missing or invalid", content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response", value = "{\"error\":\"Missing data\"}"), @ExampleObject(name = "Validation errors", value = "{\"field1\":\"Error description in field 1\", \"field2\":\"Error description in field 2\"}")})), @ApiResponse(responseCode = "404", description = "Not found if the question does not exist", content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response", value = "{\"error\":\"Question not found\"}")}))})
	@PatchMapping("/api/questions/{id}")
	public String updateQuestion(HttpServletResponse response, @RequestHeader("API-KEY") String apiKeyStr,
								 @PathVariable Long id, @RequestBody QuestionDto questionDto) throws JsonProcessingException {

		if (apiKeyService.getApiKey(apiKeyStr) == null) apiUtils.responseForAPiKeyNull(response);

		AbstractQuestion<?> q = questionService.getQuestion(id).orElse(null);

		if (q == null)
			return apiUtils.responseToError(response, HttpServletResponse.SC_NOT_FOUND, "Question not found");

		if (questionDto == null)
			return apiUtils.responseToError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing data");

		String error = apiUtils.checkErrorForQuestion(questionValidator, response, questionDto);
		if (error != null)
			return error;

		questionService.updateQuestion(id, questionDto);
		return "{ \"success\": true }";
	}

	@Operation(summary = "Delete a question", description = "Delete a question from the database")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json", examples = {@ExampleObject(name = "Example response", value = "{\"success\": true}")})}), @ApiResponse(responseCode = "404", description = "Not found if the question does not exist", content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response", value = "{\"error\":\"Question not found\"}")})), @ApiResponse(responseCode = "401", description = "Unauthorized if invalid api key", content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response", value = "{\"error\":\"Invalid API key\"}")}))})
	@DeleteMapping("/api/questions/{id}")
	public String deleteQuestion(HttpServletResponse response, @RequestHeader("API-KEY") String apiKeyStr,
								 @PathVariable Long id) throws JsonProcessingException {

		if (apiKeyService.getApiKey(apiKeyStr) == null) apiUtils.responseForAPiKeyNull(response);

		AbstractQuestion<?> q = questionService.getQuestion(id).orElse(null);
		if (q == null)
			return apiUtils.responseToError(response, HttpServletResponse.SC_NOT_FOUND, "Question not found");

		questionService.deleteQuestion(id);
		return "{ \"success\": true }";
	}
}
