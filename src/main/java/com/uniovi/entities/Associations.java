package com.uniovi.entities;

import java.util.List;

public class Associations {
	public static class PlayerRole {

		private PlayerRole() {}

		/**
		 * Add a new association between a player and a role
		 *
		 * @param player The player
		 * @param role   The role
		 */
		public static void addRole(Player player, Role role) {
			role.getPlayers().add(player);
			player.getRoles().add(role);
		}

		/**
		 * Remove an association between a player and a role
		 *
		 * @param player The player
		 * @param role   The role
		 */
		public static void removeRole(Player player, Role role) {
			role.getPlayers().remove(player);
			player.getRoles().remove(role);
		}
	}

	public static class PlayerApiKey {

		private PlayerApiKey() {}

		/**
		 * Add a new association between a player and an API key
		 *
		 * @param player The player
		 * @param apiKey The API key
		 */
		public static void addApiKey(Player player, ApiKey apiKey) {
			apiKey.setPlayer(player);
			player.setApiKey(apiKey);
		}

		/**
		 * Remove an association between a player and an API key
		 *
		 * @param player The player
		 * @param apiKey The API key
		 */
		public static void removeApiKey(Player player, ApiKey apiKey) {
			apiKey.setPlayer(null);
			player.setApiKey(null);
		}
	}

	public static class ApiKeyAccessLog {

		private ApiKeyAccessLog() {}

		/**
		 * Add a new association between an API key and an access log
		 *
		 * @param apiKey    The API key
		 * @param accessLog The access log
		 */
		public static void addAccessLog(ApiKey apiKey, RestApiAccessLog accessLog) {
			accessLog.setApiKey(apiKey);
			apiKey.getAccessLogs().add(accessLog);
		}

		/**
		 * Remove an association between an API key and an access log
		 *
		 * @param apiKey    The API key
		 * @param accessLog The access log
		 */
		public static void removeAccessLog(ApiKey apiKey, RestApiAccessLog accessLog) {
			apiKey.getAccessLogs().remove(accessLog);
			accessLog.setApiKey(null);
		}
	}

	public static class PlayerGameSession {

		private PlayerGameSession() {}

		/**
		 * Add a new association between a player and a game session
		 *
		 * @param player      The player
		 * @param gameSession The game session
		 */

		public static void addGameSession(Player player, GameSession gameSession) {
			gameSession.setPlayer(player);
			player.getGameSessions().add(gameSession);
		}

		/**
		 * Remove an association between a player and a game session
		 *
		 * @param player      The player
		 * @param gameSession The game session
		 */
		public static void removeGameSession(Player player, GameSession gameSession) {
			player.getGameSessions().remove(gameSession);
			gameSession.setPlayer(null);
		}
	}

	public static class PlayerGameSessionImage {

		private PlayerGameSessionImage() {}

		/**
		 * Add a new association between a player and a game session
		 *
		 * @param player      The player
		 * @param gameSession The game session
		 */

		public static void addGameSession(Player player, GameSessionImage gameSession) {
			gameSession.setPlayer(player);
			player.getGameSessionsImage().add(gameSession);
		}

		/**
		 * Remove an association between a player and a game session
		 *
		 * @param player      The player
		 * @param gameSession The game session
		 */
		public static void removeGameSession(Player player,
											 GameSessionImage gameSession) {
			player.getGameSessionsImage().remove(gameSession);
			gameSession.setPlayer(null);
		}
	}

	public static class QuestionAnswers {

		private QuestionAnswers() {}

		/**
		 * Add a new association between a question and an answer
		 *
		 * @param question The question
		 * @param answer   The answer
		 */
		public static void addAnswer(Question question, List<Answer> answer) {
			for (Answer a : answer) {
				a.setQuestion(question);
				if (a.isCorrect()) {
					question.setCorrectAnswer(a);
				}
			}
			question.getOptions().addAll(answer);
		}

		/**
		 * Remove an association between a question and an answer
		 *
		 * @param question The question
		 * @param answer   The answer
		 */
		public static void removeAnswer(Question question, List<Answer> answer) {
			question.getOptions().removeAll(answer);
			for (Answer a : answer) {
				a.setQuestion(null);
			}
			question.setCorrectAnswer(null);
		}
	}

	public static class QuestionsCategory {

		private QuestionsCategory() {}

		/**
		 * Add a new association between a question and a category
		 *
		 * @param question The question
		 * @param category The category
		 */
		public static void addCategory(Question question, Category category) {
			question.setCategory(category);
			category.getQuestions().add(question);
		}

		/**
		 * Remove an association between a question and a category
		 *
		 * @param question The question
		 * @param category The category
		 */
		public static void removeCategory(Question question, Category category) {
			category.getQuestions().remove(question);
			question.setCategory(null);
		}
	}

	public static class QuestionImageAnswers {

		private QuestionImageAnswers() {}

		/**
		 * Add a new association between a question and an answer
		 *
		 * @param questionImage The question
		 * @param answerImage   The answer
		 */
		public static void addAnswer(QuestionImage questionImage,
									 List<AnswerImage> answerImage) {
			for (AnswerImage a : answerImage) {
				a.setQuestion(questionImage);
				if (a.isCorrect()) {
					questionImage.setCorrectAnswer(a);
				}
			}
			questionImage.getOptions().addAll(answerImage);
		}

		/**
		 * Remove an association between a question and an answer
		 *
		 * @param questionImage The question
		 * @param answerImage   The answer
		 */
		public static void removeAnswer(QuestionImage questionImage,
										List<AnswerImage> answerImage) {
			questionImage.getOptions().removeAll(answerImage);
			for (AnswerImage a : answerImage) {
				a.setQuestion(null);
			}
			questionImage.setCorrectAnswer(null);
		}
	}

	public static class QuestionsImageCategory {

		private QuestionsImageCategory() {}

		/**
		 * Add a new association between a question and a category
		 *
		 * @param questionImage The question
		 * @param category      The category
		 */
		public static void addCategory(QuestionImage questionImage, Category category) {
			questionImage.setCategory(category);
			category.getQuestionsImageGame().add(questionImage);
		}

		/**
		 * Remove an association between a question and a category
		 *
		 * @param questionImage The question
		 * @param category      The category
		 */
		public static void removeCategory(QuestionImage questionImage,
										  Category category) {
			category.getQuestionsImageGame().remove(questionImage);
			questionImage.setCategory(null);
		}
	}
}
