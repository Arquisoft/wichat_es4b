package com.uniovi.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.dto.AnswerDto;
import com.uniovi.dto.ImageQuestionDto;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.*;
import com.uniovi.services.ApiKeyService;
import com.uniovi.services.ImageQuestionService;
import com.uniovi.services.QuestionService;
import com.uniovi.services.RestApiService;
import com.uniovi.validators.ImageQuestionValidator;
import com.uniovi.validators.QuestionValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.servlet.http.HttpServletResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SimpleErrors;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

@Tag(name = "Questions API", description = "API for managing questions")
@RestController
public class ImageQuestionsApiController {
    private final ApiKeyService apiKeyService;
    private final RestApiService restApiService;
    private final ImageQuestionValidator imageQuestionValidator;
    private final ImageQuestionService imageQuestionService;

    @Autowired
    public ImageQuestionsApiController(ApiKeyService apiKeyService, RestApiService restApiService, ImageQuestionService questionService, ImageQuestionValidator questionValidator, ImageQuestionService imageQuestionService) {
        this.apiKeyService = apiKeyService;
        this.restApiService = restApiService;
        this.imageQuestionValidator = questionValidator;
        this.imageQuestionService = imageQuestionService;
    }

    // Método para obtener preguntas con imágenes
    @Operation(summary = "Fetch questions, with optional image", description = "Fetch questions with an optional image. The image URL is included in the question object if available.")
    @Parameters({
            @Parameter(name = "apiKey", description = "API key for authentication", required = true),
            @Parameter(name = "category", description = "Category of the question. Case sensitive"),
            @Parameter(name = "statement", description = "Text contained in the statement of the question"),
            @Parameter(name = "id", description = "ID of the question")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "Example response",
                                    value = "{\n" +
                                            "   \"imageQuestions\":[\n" +
                                            "      {\n" +
                                            "         \"id\":11802,\n" +
                                            "         \"statement\":\"What is shown in this image?\",\n" +
                                            "         \"category\":{\n" +
                                            "            \"id\":1,\n" +
                                            "            \"name\":\"Geography\",\n" +
                                            "            \"description\":\"Questions about geography\"\n" +
                                            "         },\n" +
                                            "         \"imageUrl\": \"http://example.com/image.jpg\",\n" +
                                            "         \"options\":[\n" +
                                            "            {\n" +
                                            "               \"id\":46252,\n" +
                                            "               \"text\":\"Option 1\",\n" +
                                            "               \"correct\":true,\n" +
                                            "               \"question\":11802\n" +
                                            "            },\n" +
                                            "            {\n" +
                                            "               \"id\":46253,\n" +
                                            "               \"text\":\"Option 2\",\n" +
                                            "               \"correct\":false,\n" +
                                            "               \"question\":11802\n" +
                                            "            }\n" +
                                            "         ]\n" +
                                            "      }\n" +
                                            "   ]\n" +
                                            "}")}
                    )}),
            @ApiResponse(responseCode = "401", description = "Unauthorized if invalid api key",
                    content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Error response",
                            value = "{\"error\":\"Invalid API key\"}")}))
    })
@GetMapping("/api/imageQuestions")
public String getQuestions(@ParameterObject Pageable pageable, HttpServletResponse response, @RequestParam @Parameter(hidden = true) Map<String, String> params) throws JsonProcessingException {
    response.setContentType("application/json");
    ApiKey apiKey = getApiKeyFromParams(params);
    if (apiKey == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> error = Map.of("error", "Invalid API key");
        return objectMapper.writeValueAsString(error);
    }

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode root = objectMapper.createObjectNode();
    ArrayNode arrayNode = objectMapper.createArrayNode();
    List<ImageQuestion> questions = restApiService.getImageQuestions(params, pageable);
    for (ImageQuestion question : questions) {
        arrayNode.add(question.toJson());
    }
    root.set("imageQuestions", arrayNode);
    restApiService.logAccess(apiKey, "/api/imageQuestions", params);
    return root.toString();

}
// Crear pregunta con imagen
@Operation(summary = "Add a new question", description = "Add a new question to the database. The question must have a statement, a category, a related url_image and 4 options. The correct option must be marked as such.")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = {@Content(mediaType = "application/json",
                        examples = {@ExampleObject(name = "Example response",
                                value = "{\"success\": true, \"id\": 1}"
                        )}
                )}),
        @ApiResponse(responseCode = "400", description = "Bad request if the data is missing or invalid",
                content = @Content(mediaType = "application/json", examples = {
                        @ExampleObject(name = "Error response", value = "{\"error\":\"Missing data\"}"),
                        @ExampleObject(name = "Validation errors", value = "{\"field1\":\"Error description in field 1\", \"field2\":\"Error description in field 2\"}")
                }))
})
@PostMapping("/api/imageQuestions")
public String addQuestion(HttpServletResponse response, @RequestHeader("API-KEY") String apiKeyStr, @RequestBody ImageQuestionDto imageQuestionDto) throws JsonProcessingException {
    ApiKey apiKey = apiKeyService.getApiKey(apiKeyStr);
    if (apiKey == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> error = Map.of("error", "Invalid API key");
        return objectMapper.writeValueAsString(error);
    }

    if (imageQuestionDto == null) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> error = Map.of("error", "Missing data");
        return objectMapper.writeValueAsString(error);
    }

    if (imageQuestionDto.getOptions().stream().anyMatch(AnswerDto::isCorrect)) {
        imageQuestionDto.setCorrectAnswer(imageQuestionDto.getOptions().stream().filter(option -> option.isCorrect()).findFirst().get());
    }

    Errors err = new SimpleErrors(imageQuestionDto);
    imageQuestionValidator.validate(imageQuestionDto, err);

    if (err.hasErrors()) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode errorNode = objectMapper.createObjectNode();
        for (ObjectError error : err.getAllErrors()) {
            ((ObjectNode) errorNode).put(((FieldError)error).getField(), error.getDefaultMessage());
        }

        return errorNode.toString();
    }

    ImageQuestion q = imageQuestionService.addNewImageQuestion(imageQuestionDto);

    restApiService.logAccess(apiKey, "/api/imageQuestions", Map.of("apiKey", apiKeyStr, "imageQuestion", imageQuestionDto.toString()));
    return "{ \"success\": true, \"id\":" + q.getId() + " }";
}

    private ApiKey getApiKeyFromParams(Map<String, String> params) {
        if (!params.containsKey("apiKey")) {
            return null;
        }
        String apiKey = params.get("apiKey");
        return apiKeyService.getApiKey(apiKey);
    }
}
