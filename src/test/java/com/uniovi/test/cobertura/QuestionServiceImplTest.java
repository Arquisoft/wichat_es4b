package com.uniovi.test.cobertura;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.dto.AnswerDto;
import com.uniovi.dto.CategoryDto;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.*;
import com.uniovi.repositories.AnswerRepository;
import com.uniovi.repositories.QuestionRepository;
import com.uniovi.services.CategoryService;
import com.uniovi.services.impl.AnswerServiceImpl;
import com.uniovi.services.impl.QuestionGeneratorServiceImpl;
import com.uniovi.services.impl.QuestionServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class QuestionServiceImplTest {

    @InjectMocks
    private QuestionServiceImpl questionService;

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private AnswerServiceImpl answerService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private QuestionGeneratorServiceImpl questionGeneratorServiceImpl;

    private Question sampleQuestion;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category("Science", "All about science");
        sampleQuestion = new Question();
        sampleQuestion.setStatement("What is the boiling point of water?");
        sampleQuestion.setLanguage("en");
        Associations.QuestionsCategory.addCategory(sampleQuestion, sampleCategory);

        Answer answer1 = new Answer("100°C", true);
        Answer answer2 = new Answer("90°C", false);
        Associations.QuestionAnswers.addAnswer(sampleQuestion, List.of(answer1, answer2));
    }

    @Test
    void shouldAddNewQuestion() {
        when(categoryService.getCategoryByName(anyString())).thenReturn(sampleCategory);

        QuestionDto dto = new QuestionDto();
        dto.setStatement("New Question?");
        dto.setLanguage("en");
        dto.setCategory(new CategoryDto("Science", "Science Desc"));

        AnswerDto a1 = new AnswerDto("Asturias", false);
        AnswerDto a2 = new AnswerDto("Cataluña", false);
        AnswerDto a3 = new AnswerDto("Madrid", false);
        AnswerDto a4 = new AnswerDto("Benidorm", true);
        List<AnswerDto> lanswer = Arrays.asList(a1, a2, a3, a4);
        dto.setOptions(lanswer);
        dto.setCorrectAnswer(a1);

        Question result = questionService.addNewQuestion(dto);

        assertThat(result).isNotNull();
        assertThat(result.getStatement()).isEqualTo("New Question?");
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    void shouldGetAllQuestions() {
        when(questionRepository.findAll()).thenReturn(List.of(sampleQuestion));

        List<Question> questions = questionService.getAllQuestions();

        assertThat(questions).isNotEmpty();
        assertThat(questions.get(0).getStatement()).isEqualTo(sampleQuestion.getStatement());
    }

    @Test
    void shouldDeleteQuestion() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(sampleQuestion));

        questionService.deleteQuestion(1L);

        verify(answerRepository, times(1)).deleteAll(anyList());
        verify(questionRepository, times(1)).delete(any(Question.class));
    }
}