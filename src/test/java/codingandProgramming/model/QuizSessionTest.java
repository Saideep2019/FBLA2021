package codingandProgramming.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class QuizSessionTest {

	@Test
	void sessionSelectsExactlyFiveQuestions() {
		QuizSession session = sessionWithSeed(10L);

		assertEquals(5, session.getSelectedQuestions().size());
	}

	@Test
	void selectedQuestionsAreDistinct() {
		QuizSession session = sessionWithSeed(20L);

		Set<Integer> selectedIds = idsOf(session);
		assertEquals(5, selectedIds.size());
	}

	@Test
	void selectedQuestionIdsComeFromTheRepository() {
		QuizSession session = sessionWithSeed(30L);
		Set<Integer> repositoryIds = Set.of(101, 205, 309, 412, 518, 623, 731, 844);

		assertTrue(repositoryIds.containsAll(idsOf(session)));
	}

	@Test
	void seededRandomnessProducesDeterministicSelections() {
		QuizSession firstSession = sessionWithSeed(12345L);
		QuizSession secondSession = sessionWithSeed(12345L);

		assertEquals(idsInOrder(firstSession), idsInOrder(secondSession));
	}

	@Test
	void correctAnswerIncreasesTheScore() {
		QuizSession session = sessionWithSeed(40L);
		String correctAnswer = session.getCurrentQuestion().getCorrectAnswer();

		QuizResult result = session.submitAnswer(correctAnswer);

		assertTrue(result.isCorrect());
		assertEquals(1, session.getScore());
	}

	@Test
	void incorrectAnswerDoesNotIncreaseTheScore() {
		QuizSession session = sessionWithSeed(50L);

		QuizResult result = session.submitAnswer("not the answer");

		assertFalse(result.isCorrect());
		assertEquals(0, session.getScore());
	}

	@Test
	void answerComparisonIsCaseInsensitive() {
		Question caseSensitiveLookingQuestion = new Question(101, "Question 101",
				Question.DisplayType.FOUR_BUTTONS, "MiXeD Answer", List.of("MiXeD Answer", "Other"));
		QuizSession session = new QuizSession(repositoryWith(caseSensitiveLookingQuestion, 205, 309, 412, 518),
				new Random(60L));

		while (session.getCurrentQuestion().getId() != 101) {
			session.submitAnswer("wrong");
		}

		QuizResult result = session.submitAnswer("mixed answer");

		assertTrue(result.isCorrect());
	}

	@Test
	void blankAnswerIsRecordedAsIncorrectToCharacterizeLegacyBehavior() {
		QuizSession session = sessionWithSeed(70L);

		QuizResult result = session.submitAnswer("");

		assertEquals("", result.getSelectedAnswer());
		assertFalse(result.isCorrect());
		assertEquals(0, session.getScore());
	}

	@Test
	void completingFiveQuestionsEndsTheSession() {
		QuizSession session = sessionWithSeed(80L);

		for (int questionNumber = 0; questionNumber < QuizSession.SESSION_LENGTH; questionNumber++) {
			assertFalse(session.isComplete());
			session.submitAnswer("wrong");
		}

		assertTrue(session.isComplete());
		assertEquals(5, session.getReport().getQuestionsAttempted());
		assertThrows(IllegalStateException.class, session::getCurrentQuestion);
		assertThrows(IllegalStateException.class, () -> session.submitAnswer("another answer"));
	}

	private QuizSession sessionWithSeed(long seed) {
		return new QuizSession(this::repositoryQuestions, new Random(seed));
	}

	private List<Question> repositoryQuestions() {
		return List.of(question(101), question(205), question(309), question(412), question(518), question(623),
				question(731), question(844));
	}

	private QuestionRepository repositoryWith(Question firstQuestion, int... otherIds) {
		return () -> {
			java.util.ArrayList<Question> questions = new java.util.ArrayList<>();
			questions.add(firstQuestion);
			for (int id : otherIds) {
				questions.add(question(id));
			}
			return questions;
		};
	}

	private Question question(int id) {
		return new Question(id, "Question " + id, Question.DisplayType.FOUR_BUTTONS, "Answer " + id,
				List.of("Answer " + id, "Other"));
	}

	private Set<Integer> idsOf(QuizSession session) {
		return session.getSelectedQuestions().stream().map(Question::getId).collect(Collectors.toSet());
	}

	private List<Integer> idsInOrder(QuizSession session) {
		return session.getSelectedQuestions().stream().map(Question::getId).toList();
	}
}
