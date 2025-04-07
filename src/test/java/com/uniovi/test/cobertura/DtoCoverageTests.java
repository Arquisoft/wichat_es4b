package com.uniovi.test.cobertura;

import com.uniovi.dto.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unitTest coverage for Dtos")
@TestMethodOrder(OrderAnnotation.class)
public class DtoCoverageTests {

    // Test de cobertura de los DTOs. Reservado de la 525 a la 549

    @Test
    @Order(525)
    public void testAnswerDto() {
        // AnswerDto
        AnswerDto answerDto = new AnswerDto();
        answerDto.setText("Answer 1");
        answerDto.setCorrect(true);
        assertEquals("Answer 1", answerDto.getText());
        assertTrue(answerDto.isCorrect());
        assertNotNull(answerDto.toString());

        AnswerDto answerDto2 = new AnswerDto("Answer 2", false);
        assertEquals("Answer 2", answerDto2.getText());
        assertFalse(answerDto2.isCorrect());
    }

    @Test
    @Order(526)
    public void testCategoryDto() {
        // CategoryDto
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("History");
        categoryDto.setDescription("Description");
        categoryDto.setQuestions(List.of());
        categoryDto.setQuestionsImage(List.of());
        assertEquals("History", categoryDto.getName());
        assertEquals("Description", categoryDto.getDescription());

        CategoryDto categoryDto2 = new CategoryDto("Science", "All about science");
        assertEquals("Science", categoryDto2.getName());
        assertEquals("All about science", categoryDto2.getDescription());
    }

    @Test
    @Order(527)
    public void testPlayerDto() {
        // PlayerDto
        PlayerDto playerDto = new PlayerDto();
        playerDto.setUsername("user1");
        playerDto.setEmail("user1@mail.com");
        playerDto.setPassword("1234");
        playerDto.setPasswordConfirm("1234");
        playerDto.setRoles(new String[]{"ROLE_USER"});
        assertEquals("user1", playerDto.getUsername());
        assertNotNull(playerDto.toString());
    }

    @Test
    @Order(528)
    public void testQuestionDto() {
        // QuestionDto
        AnswerDto answerDto1 = new AnswerDto("Answer 1", true);
        AnswerDto answerDto2 = new AnswerDto("Answer 2", false);

        CategoryDto categoryDto = new CategoryDto("History", "Description");

        QuestionDto questionDto = new QuestionDto();
        questionDto.setStatement("What is 2 + 2?");
        questionDto.setOptions(List.of(answerDto1, answerDto2));
        questionDto.setCorrectAnswer(answerDto1);
        questionDto.setCategory(categoryDto);
        questionDto.setLanguage("en");

        assertEquals("What is 2 + 2?", questionDto.getStatement());
        assertEquals("en", questionDto.getLanguage());
        assertNotNull(questionDto.toString());
    }

    @Test
    @Order(529)
    public void testQuestionImageDto() {
        // QuestionImageDto
        AnswerDto answerDto1 = new AnswerDto("Answer 1", true);
        CategoryDto categoryDto = new CategoryDto("History", "Description");

        QuestionImageDto questionImageDto = new QuestionImageDto();
        questionImageDto.setStatement("What is shown?");
        questionImageDto.setOptions(List.of(answerDto1));
        questionImageDto.setCorrectAnswer(answerDto1);
        questionImageDto.setCategory(categoryDto);
        questionImageDto.setLanguage("es");
        questionImageDto.setImageUrl("http://example.com/image.png");

        assertEquals("http://example.com/image.png", questionImageDto.getImageUrl());
        assertNotNull(questionImageDto.toString());
    }

    @Test
    @Order(530)
    public void testRoleDto() {
        // RoleDto
        RoleDto roleDto = new RoleDto("ROLE_ADMIN");
        assertEquals("ROLE_ADMIN", roleDto.getName());
    }
}
