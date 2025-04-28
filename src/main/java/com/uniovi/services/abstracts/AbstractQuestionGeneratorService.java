package com.uniovi.services.abstracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniovi.entities.Category;
import com.uniovi.services.QuestionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public abstract class AbstractQuestionGeneratorService<S extends QuestionService<?, ?>> {

	public final String JSONFilePath;

	protected final S questionService;
	protected final Environment environment;
	protected final Deque<QuestionType> types = new ArrayDeque<>();
	protected JsonNode json;
	protected boolean started;
	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected AbstractQuestionGeneratorService(S questionService, Environment environment,
											   String JSONFilePath) throws IOException {
		this.questionService = questionService;
		this.environment     = environment;
		this.JSONFilePath    = JSONFilePath;
		setQuestionGeneratorService();
		parseQuestionTypes();
		this.started = true;
	}

	protected abstract void setQuestionGeneratorService();


	private void parseQuestionTypes() throws IOException {
		if (json == null) {
			Resource resource = new ClassPathResource(JSONFilePath);
			ObjectMapper objectMapper = new ObjectMapper();
			json = objectMapper.readTree(resource.getInputStream());
		}
		JsonNode categories = json.findValue("categories");
		for (JsonNode category : categories) {
			String categoryName = category.get("name").textValue();
			Category cat = new Category(categoryName);
			JsonNode questionsNode = category.findValue("questions");
			for (JsonNode question : questionsNode) {
				types.push(new QuestionType(question, cat));
			}
		}
	}

	private void generateAllQuestions() throws IOException {
		started = true;
		resetGeneration();
	}

	@Transactional
	public void generateQuestions() throws IOException, InterruptedException {
		if (types.isEmpty()) {
			return;
		}

		if (started) {
			started = false;
			questionService.deleteAllQuestions();
		}

		if (Arrays.stream(environment.getActiveProfiles())
				.anyMatch(env -> (env.equalsIgnoreCase("test")))) {
			log.info("Test profile active, skipping sample data insertion");
			return;
		}

		processQuestions();

	}

	@Transactional
	protected abstract void processQuestions() throws IOException, InterruptedException;


	@Transactional
	public abstract void generateTestQuestions() throws IOException, InterruptedException;

	@Transactional
	public abstract void generateTestQuestions(String cat);

	public void setJsonGeneration(JsonNode json) {
		this.json = json;
	}

	public void resetGeneration() throws IOException {
		types.clear();
		parseQuestionTypes();
	}

	public JsonNode getJsonGeneration() {
		return json;
	}

	@Getter
	@AllArgsConstructor
	protected static class QuestionType {
		private final JsonNode question;
		private final Category category;
	}
}
