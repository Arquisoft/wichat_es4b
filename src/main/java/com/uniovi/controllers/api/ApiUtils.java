package com.uniovi.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.dto.AnswerDto;
import com.uniovi.dto.PlayerDto;
import com.uniovi.dto.QuestionDto;
import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.ApiKey;
import com.uniovi.entities.Role;
import com.uniovi.services.ApiKeyService;
import com.uniovi.validators.QuestionValidator;
import com.uniovi.validators.SignUpValidator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SimpleErrors;

import java.util.List;
import java.util.Map;

public class ApiUtils {


	ApiKey getApiKeyFromParams(ApiKeyService apiKeyService, Map<String, String> params) {
		return (!params.containsKey("apiKey")) ? null : apiKeyService.getApiKey(
				params.get("apiKey"));
	}

	String resToError(HttpServletResponse response, int status, String message)
			throws JsonProcessingException {
		response.setStatus(status);
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> error = Map.of("error", message);
		return objectMapper.writeValueAsString(error);
	}

	String responseForAPiKeyNull(HttpServletResponse response)
			throws JsonProcessingException {
		return resToError(response, HttpServletResponse.SC_UNAUTHORIZED,
						  "Invalid API key");
	}

	String checkErrorForPassword(SignUpValidator signUpValidator,
								 HttpServletResponse response, PlayerDto playerDto) {
		playerDto.setPasswordConfirm(playerDto.getPassword());

		Errors err = new SimpleErrors(playerDto);
		signUpValidator.validate(playerDto, err);

		return (err.hasErrors()) ? resToError(response, err) : null;
	}

	boolean isAdmin(ApiKey apiKey) {
		return apiKey != null &&
				apiKey.getPlayer().getRoles().contains(new Role("ROLE_ADMIN"));
	}

	boolean isAdminOrOwn(ApiKey apiKey, Long id) {
		return apiKey.getPlayer().getId().equals(id) || isAdmin(apiKey);
	}

	String checkErrorForImageQuestion(QuestionValidator questionValidator,
									  QuestionImageDto question,
									  HttpServletResponse response)
			throws JsonProcessingException {
		//Compruebo los casos genericos
		String err = checkErrorForBasicQuestion(questionValidator, question, response);
		if (err != null) return err;

		// Compruebo el campo de la url de la imagen
		return checkRequiredField(question.getImageUrl(), "question image URL", question,
								  response);
	}

	String checkErrorForBasicQuestion(QuestionValidator questionValidator,
									  QuestionDto question, HttpServletResponse response)
			throws JsonProcessingException {

		// Comprobamos los campos requeridos y retornamos errores si alguno falta
		String errorMessage;

		errorMessage = checkRequiredField(question.getStatement(), "question statement",
										  question, response);
		if (errorMessage != null) return errorMessage;

		errorMessage = checkRequiredField(question.getLanguage(), "question language",
										  question, response);
		if (errorMessage != null) return errorMessage;

		errorMessage = checkRequiredField(question.getCategory(), "question category",
										  question, response);
		if (errorMessage != null) return errorMessage;

		errorMessage = checkRequiredField(question.getOptions(), "question options",
										  question, response);
		if (errorMessage != null) return errorMessage;

		return validateAndSetCorrectAnswer(question, questionValidator, response);
	}

	public String validateAndSetCorrectAnswer(QuestionDto question,
											  QuestionValidator questionValidator,
											  HttpServletResponse response) {
		// Establecemos la respuesta correcta
		question.getOptions().stream().filter(AnswerDto::isCorrect).findFirst()
				.ifPresent(question::setCorrectAnswer);

		// Validamos con el validador
		Errors err = new SimpleErrors(question);
		questionValidator.validate(question, err);

		return (err.hasErrors()) ? resToError(response, err) : null;
	}


	// Función auxiliar para verificar si un campo es obligatorio

	private String checkRequiredField(Object field, String description,
									  QuestionDto question, HttpServletResponse response)
			throws JsonProcessingException {
		// Verificación para los campos String (nulos o vacíos)
		switch (field) {
			case String s -> {
				if (s.isEmpty())
					return createErrorResponse(description, question, "Missing data",
											   response);
			}
			// Verificación para los campos List (nulos o vacíos)
			case List<?> list -> {
				if (list.isEmpty())
					return createErrorResponse(description, question, "Missing data",
											   response);
			}
			// Verificación para objetos genéricos (solo nulos)
			case null -> {
				return createErrorResponse(description, question, "Missing data",
										   response);
			}
			default -> {
			}
		}

		return null;
	}

	private String createErrorResponse(String description, QuestionDto question,
									   String error, HttpServletResponse response)
			throws JsonProcessingException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		return new ObjectMapper().writeValueAsString(
				Map.of("success", false, "error", error, "description", description,
					   "question", question));
	}


	// ---------------- METODOS COMPLEMENTARIOS ----------------
	private String resToError(HttpServletResponse response, Errors err) {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode errorNode = objectMapper.createObjectNode();
		for (ObjectError error : err.getAllErrors())
			((ObjectNode) errorNode).put(((FieldError) error).getField(),
										 error.getDefaultMessage());

		return errorNode.toString();
	}
}
