package com.uniovi.test.cobertura;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.dto.CategoryDto;
import com.uniovi.dto.QuestionImageDto;
import com.uniovi.entities.AnswerImage;
import com.uniovi.entities.Associations;
import com.uniovi.entities.Category;
import com.uniovi.entities.QuestionImage;
import com.uniovi.repositories.AnswerImageRepository;
import com.uniovi.repositories.QuestionImageRepository;
import com.uniovi.services.LlmService;
import com.uniovi.services.impl.AnswerServiceImageImpl;
import com.uniovi.services.impl.CategoryServiceImpl;
import com.uniovi.services.impl.QuestionImageGeneratorServiceImpl;
import com.uniovi.services.impl.QuestionImageServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class QuestionImageServiceImplTest {

    @InjectMocks
    private QuestionImageServiceImpl questionService;

    @Mock
    private QuestionImageRepository questionRepository;
    @Mock
    private AnswerImageRepository answerRepository;
    @Mock
    private CategoryServiceImpl categoryService;
    @Mock
    private AnswerServiceImageImpl answerService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private LlmService llmService;
    @Mock
    private QuestionImageGeneratorServiceImpl questionGeneratorService;

    private QuestionImage sampleQuestion;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category("Geography", "Maps and locations");
        sampleQuestion = new QuestionImage();
        sampleQuestion.setStatement("Where is this monument?");
        sampleQuestion.setLanguage("en");
        sampleQuestion.setImageUrl("http://example.com/image.jpg");

        Associations.QuestionsImageCategory.addCategory(sampleQuestion, sampleCategory);

        AnswerImage correctAnswer = new AnswerImage("Paris", true);
        AnswerImage wrongAnswer = new AnswerImage("London", false);

        Associations.QuestionImageAnswers.addAnswer(sampleQuestion, List.of(correctAnswer, wrongAnswer));
    }



    @Test
    void shouldGetAllQuestions() {
        when(questionRepository.findAll()).thenReturn(List.of(sampleQuestion));

        List<QuestionImage> questions = questionService.getAllQuestions();

        assertThat(questions).isNotEmpty();
        assertThat(questions.get(0).getStatement()).isEqualTo(sampleQuestion.getStatement());
    }


    @Test
    void shouldDeleteQuestion() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(sampleQuestion));

        questionService.deleteQuestion(1L);

        verify(answerRepository, times(1)).deleteAll(anyList());
        verify(questionRepository, times(1)).delete(any(QuestionImage.class));
    }



    @Test
    void shouldGetHintForImageQuestion() {
        when(llmService.sendQuestionToLLM(anyString(), anyString(), anyList())).thenReturn("This is a hint");

        String hint = questionService.getHintForImageQuestion(sampleQuestion, "AI_MODEL");

        assertThat(hint).isEqualTo("This is a hint");
        verify(llmService, times(1)).sendQuestionToLLM(anyString(), anyString(), anyList());
    }
}