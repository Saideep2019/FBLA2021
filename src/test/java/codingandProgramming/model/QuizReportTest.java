package codingandProgramming.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

class QuizReportTest {
	@Test
	void percentageIsCorrectForEveryPossibleFiveQuestionScore() {
		for (int correctAnswers = 0; correctAnswers <= QuizSession.SESSION_LENGTH; correctAnswers++) {
			QuizSession session = new QuizSession(
					() -> List.of(question(1), question(2), question(3), question(4), question(5)), new Random(90L));

			for (int answerNumber = 0; answerNumber < QuizSession.SESSION_LENGTH; answerNumber++) {
				String answer = answerNumber < correctAnswers ? session.getCurrentQuestion().getCorrectAnswer() : "wrong";
				session.submitAnswer(answer);
			}

			assertEquals(correctAnswers * 20, session.getReport().getPercentageCorrect(),
					"percentage for score " + correctAnswers);
		}
	}

	@Test
	void reportRowsContainQuestionCorrectAnswerSelectedAnswerAndCorrectness() {
		QuestionRepository repository = () -> List.of(question(1), question(2), question(3), question(4), question(5));
		QuizSession session = new QuizSession(repository, new Random(90L));
		Question currentQuestion = session.getCurrentQuestion();

		session.submitAnswer(currentQuestion.getCorrectAnswer().toUpperCase());
		QuizResult row = session.getReport().getRows().get(0);

		assertEquals(currentQuestion.getText(), row.getQuestionText());
		assertEquals(currentQuestion.getCorrectAnswer(), row.getCorrectAnswer());
		assertEquals(currentQuestion.getCorrectAnswer().toUpperCase(), row.getSelectedAnswer());
		assertTrue(row.isCorrect());
	}

	private Question question(int id) {
		return new Question(id, "Question " + id, Question.DisplayType.DROP_DOWN, "answer " + id,
				List.of("first", "second", "third", "fourth"));
	}
}
