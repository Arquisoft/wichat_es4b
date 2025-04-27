package com.uniovi.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniovi.dto.QuestionDto;
import com.uniovi.entities.Answer;
import com.uniovi.entities.Associations;
import com.uniovi.entities.Category;
import com.uniovi.entities.Question;
import com.uniovi.repositories.AnswerRepository;
import com.uniovi.repositories.QuestionRepository;
import com.uniovi.services.CategoryService;
import com.uniovi.services.QuestionService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class QuestionServiceImpl implements QuestionService<Question, QuestionDto> {

	private final CategoryService categoryService;
	private final AnswerServiceImpl answerService;
	private final EntityManager entityManager;
	private final QuestionRepository questionRepository;
	private final AnswerRepository answerRepository;

	@Setter
	private QuestionGeneratorServiceImpl questionGeneratorServiceImpl;

	private final Random random = new SecureRandom();

	public QuestionServiceImpl(QuestionRepository questionRepository, CategoryService categoryService,
							   AnswerServiceImpl answerService, AnswerRepository answerRepository,
							   EntityManager entityManager) {
		this.questionRepository = questionRepository;
		this.categoryService = categoryService;
		this.answerService = answerService;
		this.answerRepository = answerRepository;
		this.entityManager = entityManager;
	}

	@Override
	public void addNewQuestion(Question question) {
		questionRepository.save(question);
	}

	@Override
	public Question addNewQuestion(QuestionDto question) {
		Category category = categoryService.getCategoryByName(question.getCategory().getName());
		if (category == null) {
			categoryService.addNewCategory(new Category(question.getCategory().getName(), question.getCategory().getDescription()));
			category = categoryService.getCategoryByName(question.getCategory().getName());
		}

		List<Answer> answers = new ArrayList<>();
		for (int i = 0; i < question.getOptions().size(); i++) {
			Answer a = new Answer();
			a.setText(question.getOptions().get(i).getText());
			a.setCorrect(question.getOptions().get(i).isCorrect());
			answerService.addNewAnswer(a);
			answers.add(a);
		}

		Question q = new Question();
		q.setStatement(question.getStatement());
		q.setLanguage(question.getLanguage());
		Associations.QuestionsCategory.addCategory(q, category);
		Associations.QuestionAnswers.addAnswer(q, answers);
		addNewQuestion(q);

		return q;
	}

	@Override
	public List<Question> getAllQuestions() {
		return new ArrayList<>(questionRepository.findAll());
	}

	@Override
	public Page<Question> getQuestions(Pageable pageable) {
		return questionRepository.findByLanguage(pageable, LocaleContextHolder.getLocale().getLanguage());
	}

	@Override
	public Optional<Question> getQuestion(Long id) {
		return questionRepository.findById(id);
	}

	@Override
	public List<Question> getRandomQuestions(int num) {
		List<Question> allQuestions = questionRepository.findAll().stream()
				.filter(question -> question.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())).toList();
		List<Question> res = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			int idx = random.nextInt(allQuestions.size());
			while (allQuestions.get(idx).hasEmptyOptions() || res.contains(allQuestions.get(idx))) {
				idx = random.nextInt(allQuestions.size());
			}
			res.add(allQuestions.get(idx));
		}
		return res;
	}

	@Override
	public boolean checkAnswer(Long idquestion, Long idanswer) {
		Optional<Question> q = questionRepository.findById(idquestion);
		return q.map(question -> question.getCorrectAnswer().getId().equals(idanswer)).orElse(false);
	}

	@Override
	public List<Question> getQuestionsByCategory(Pageable pageable, Category category, String lang) {
		return questionRepository.findByCategoryAndLanguage(pageable, category, lang).toList();
	}

	@Override
	public List<Question> getQuestionsByStatement(Pageable pageable, String statement, String lang) {
		return questionRepository.findByStatementAndLanguage(pageable, statement, lang).toList();
	}

	@Override
	public void updateQuestion(Long id, QuestionDto questionDto) {
		Optional<Question> q = questionRepository.findById(id);
		if (q.isPresent()) {
			entityManager.clear();
			Question question = q.get();
			question.setStatement(questionDto.getStatement());
			question.setLanguage(questionDto.getLanguage());
			Category category = categoryService.getCategoryByName(questionDto.getCategory().getName());
			if (category == null) {
				categoryService.addNewCategory(new Category(questionDto.getCategory().getName(), questionDto.getCategory().getDescription()));
				category = categoryService.getCategoryByName(questionDto.getCategory().getName());
			}

			Associations.QuestionsCategory.removeCategory(question, question.getCategory());

			for (int i = 0; i < questionDto.getOptions().size(); i++) {
				Answer a = question.getOption(i);
				a.setText(questionDto.getOptions().get(i).getText());
				a.setCorrect(questionDto.getOptions().get(i).isCorrect());
			}

			Associations.QuestionsCategory.addCategory(question, category);
			questionRepository.save(question);
		}
	}

	@Override
	@Transactional
	public void deleteQuestion(Long id) {
		Optional<Question> q = questionRepository.findById(id);
		if (q.isPresent()) {
			Question question = q.get();
			answerRepository.deleteAll(question.getOptions());
			Associations.QuestionAnswers.removeAnswer(question, question.getOptions());
			Associations.QuestionsCategory.removeCategory(question, question.getCategory());
			q.get().setCorrectAnswer(null);
			questionRepository.delete(question);
		}
	}

	@Override
	public void deleteAllQuestions() throws IOException {
		questionGeneratorServiceImpl.resetGeneration();
		questionRepository.deleteAll();
	}

	@Override
	public int getSecondsPerQuestion() {
		return SECONDS_PER_QUESTION;
	}

	@Override
	public void setJsonGenerator(JsonNode json) {
		questionGeneratorServiceImpl.setJsonGeneration(json);
	}

	@Override
	public JsonNode getJsonGenerator() {
		return questionGeneratorServiceImpl.getJsonGeneration();
	}

}
