package com.uniovi.test.cobertura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniovi.controllers.api.ApiUtils;
import com.uniovi.dto.*;
import com.uniovi.entities.ApiKey;
import com.uniovi.entities.Player;
import com.uniovi.entities.Role;
import com.uniovi.services.ApiKeyService;
import com.uniovi.validators.QuestionValidator;
import com.uniovi.validators.SignUpValidator;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApiUtilsTest {

    private ApiUtils apiUtils;
    private ApiKeyService apiKeyService;
    private QuestionValidator questionValidator;
    private SignUpValidator signUpValidator;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        apiUtils = new ApiUtils();
        apiKeyService = mock(ApiKeyService.class);
        questionValidator = mock(QuestionValidator.class);
        signUpValidator = mock(SignUpValidator.class);
        response = new MockHttpServletResponse();
    }

    @Test
    void testGetApiKeyFromParams() {
        ApiKey expectedApiKey = new ApiKey();
        when(apiKeyService.getApiKey("test-key")).thenReturn(expectedApiKey);

        Map<String, String> params = Map.of("apiKey", "test-key");
        ApiKey result = apiUtils.getApiKeyFromParams(apiKeyService, params);

        assertThat(result).isEqualTo(expectedApiKey);
    }

    @Test
    void testGetApiKeyFromParamsMissing() {
        Map<String, String> params = Map.of();
        ApiKey result = apiUtils.getApiKeyFromParams(apiKeyService, params);

        assertThat(result).isNull();
    }

    @Test
    void testResToError() throws JsonProcessingException {
        String json = apiUtils.resToError(response, HttpServletResponse.SC_NOT_FOUND, "Not Found");

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        assertThat(json).contains("Not Found");
    }

    @Test
    void testResponseForApiKeyNull() throws JsonProcessingException {
        String json = apiUtils.responseForAPiKeyNull(response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(json).contains("Invalid API key");
    }

    @Test
    void testCheckErrorForPassword_NoErrors() throws JsonProcessingException {
        PlayerDto playerDto = new PlayerDto();
        playerDto.setPassword("test");
        playerDto.setPasswordConfirm("test");

        doAnswer(invocation -> null).when(signUpValidator).validate(any(), any());

        String result = apiUtils.checkErrorForPassword(signUpValidator, response, playerDto);

        assertThat(result).isNull();
    }

    @Test
    void testIsAdmin_True() {
        Role adminRole = new Role("ROLE_ADMIN");
        Player player = new Player();
        player.setRoles(Set.of(adminRole));
        ApiKey apiKey = new ApiKey();
        apiKey.setPlayer(player);

        boolean result = apiUtils.isAdmin(apiKey);

        assertThat(result).isTrue();
    }

    @Test
    void testIsAdmin_False() {
        Player player = new Player();
        player.setRoles(Set.of(new Role("ROLE_USER")));
        ApiKey apiKey = new ApiKey();
        apiKey.setPlayer(player);

        boolean result = apiUtils.isAdmin(apiKey);

        assertThat(result).isFalse();
    }

    @Test
    void testIsAdminOrOwn_Admin() {
        Player player = new Player();
        player.setId(1L);
        player.setRoles(Set.of(new Role("ROLE_ADMIN")));
        ApiKey apiKey = new ApiKey();
        apiKey.setPlayer(player);

        boolean result = apiUtils.isAdminOrOwn(apiKey, 2L);

        assertThat(result).isTrue();
    }

    @Test
    void testIsAdminOrOwn_OwnUser() {
        Player player = new Player();
        player.setId(1L);
        ApiKey apiKey = new ApiKey();
        apiKey.setPlayer(player);

        boolean result = apiUtils.isAdminOrOwn(apiKey, 1L);

        assertThat(result).isTrue();
    }

    @Test
    void testIsAdminOrOwn_False() {
        Player player = new Player();
        player.setId(1L);
        player.setRoles(Set.of(new Role("ROLE_USER")));
        ApiKey apiKey = new ApiKey();
        apiKey.setPlayer(player);

        boolean result = apiUtils.isAdminOrOwn(apiKey, 2L);

        assertThat(result).isFalse();
    }

    @Test
    void testCheckErrorForImageQuestion_MissingImageUrl() throws JsonProcessingException {
        QuestionImageDto questionImageDto = new QuestionImageDto();
        questionImageDto.setStatement("What is this?");
        questionImageDto.setLanguage("en");
        questionImageDto.setCategory(new CategoryDto("Math","Math"));
        questionImageDto.setOptions(List.of(new AnswerDto()));

        String result = apiUtils.checkErrorForImageQuestion(questionValidator, questionImageDto, response);

        assertThat(result).contains("question image URL");
    }

    @Test
    void testValidateAndSetCorrectAnswer_NoErrors() throws JsonProcessingException {
        QuestionDto questionDto = new QuestionDto();
        AnswerDto correctAnswer = new AnswerDto();
        correctAnswer.setCorrect(true);
        questionDto.setOptions(List.of(correctAnswer));

        when(questionValidator.supports(QuestionDto.class)).thenReturn(true);
        doAnswer(invocation -> null).when(questionValidator).validate(any(), any());

        String result = apiUtils.validateAndSetCorrectAnswer(questionDto, questionValidator, response);

        assertThat(result).isNull();
        assertThat(questionDto.getCorrectAnswer()).isNotNull();
    }
}
