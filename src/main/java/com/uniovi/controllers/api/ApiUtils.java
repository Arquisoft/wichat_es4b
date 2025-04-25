package com.uniovi.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.dto.AnswerDto;
import com.uniovi.dto.PlayerDto;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.ApiKey;
import com.uniovi.services.ApiKeyService;
import com.uniovi.validators.QuestionValidator;
import com.uniovi.validators.SignUpValidator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SimpleErrors;

import java.util.Map;

public class ApiUtils {


	ApiKey getApiKeyFromParams(ApiKeyService apiKeyService, Map<String, String> params) {
		return (!params.containsKey("apiKey")) ? null : apiKeyService.getApiKey(params.get("apiKey"));
	}

	String responseToError(HttpServletResponse response, int status, String message) throws JsonProcessingException {
		response.setStatus(status);
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> error = Map.of("error", message);
		return objectMapper.writeValueAsString(error);
	}

	String responseWithAPiKeyNull(HttpServletResponse response) throws JsonProcessingException {
		return responseToError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
	}

	String checkErrorForPassword(SignUpValidator signUpValidator, HttpServletResponse response, PlayerDto playerDto) {
		playerDto.setPasswordConfirm(playerDto.getPassword());

		Errors err = new SimpleErrors(playerDto);
		signUpValidator.validate(playerDto, err);

		return (err.hasErrors()) ? responseToError(response, err) : null;
	}

	String checkErrorForQuestion(QuestionValidator questionValidator, HttpServletResponse response, QuestionDto questionDto) {
		if (questionDto.getOptions() != null)
			questionDto.getOptions().stream().filter(AnswerDto::isCorrect).findFirst().ifPresent(questionDto::setCorrectAnswer);

		Errors err = new SimpleErrors(questionDto);
		questionValidator.validate(questionDto, err);

		return (err.hasErrors()) ? responseToError(response, err) : null;
	}

	// ---------------- METODOS COMPLEMENTARIOS ----------------
	private String responseToError(HttpServletResponse response, Errors err) {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode errorNode = objectMapper.createObjectNode();
		for (ObjectError error : err.getAllErrors())
			((ObjectNode) errorNode).put(((FieldError) error).getField(), error.getDefaultMessage());

		return errorNode.toString();
	}
}
