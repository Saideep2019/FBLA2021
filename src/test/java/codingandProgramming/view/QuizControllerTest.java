package codingandProgramming.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import codingandProgramming.model.Question;
import codingandProgramming.model.QuizReport;
import codingandProgramming.model.QuizSession;

class QuizControllerTest {

	@Test
	void completesFiveQuestionQuizFromFakeRepositoryAndReportsExactlyOnce() throws Exception {
		QuizSession session = sessionFromFakeRepository();
		FakeQuizView view = new FakeQuizView();
		QuizController controller = new QuizController(session, view, "Test Student");

		onEdt(() -> {
			controller.start();
			controller.start();
			assertEquals(1, view.questionDisplayCount);

			for (int questionNumber = 0; questionNumber < QuizSession.SESSION_LENGTH; questionNumber++) {
				view.selectedAnswer = view.currentQuestion.getCorrectAnswer().toUpperCase();
				controller.submitCurrentAnswer();
			}

			assertTrue(session.isComplete());
			assertEquals(5, session.getScore());
			assertEquals(5, session.getReport().getQuestionsAttempted());
			assertEquals(5, view.questionDisplayCount);
			assertEquals(1, view.reportCount);
			assertEquals(100, view.report.getPercentageCorrect());
			assertEquals("Test Student", view.reportStudentName);
			assertFalse(view.submissionEnabled);
			assertTrue(view.answerCleared);
			assertTrue(view.allCallsOnEdt);

			controller.submitCurrentAnswer();
			controller.submitCurrentAnswer();
			assertEquals(5, session.getReport().getQuestionsAttempted());
			assertEquals(5, session.getScore());
			assertEquals(1, view.reportCount);
		});
	}

	@Test
	void blankAnswerDoesNotAdvanceOrScore() throws Exception {
		QuizSession session = sessionFromFakeRepository();
		FakeQuizView view = new FakeQuizView();
		QuizController controller = new QuizController(session, view, "Student");

		onEdt(() -> {
			controller.start();
			Question firstQuestion = view.currentQuestion;
			view.selectedAnswer = "   ";
			controller.submitCurrentAnswer();

			assertEquals(firstQuestion, view.currentQuestion);
			assertEquals(1, view.questionDisplayCount);
			assertEquals(1, view.validationErrorCount);
			assertEquals("Please select or enter an answer before continuing.", view.validationMessage);
			assertEquals(0, session.getReport().getQuestionsAttempted());
			assertEquals(0, session.getScore());
			assertTrue(view.submissionEnabled);
		});
	}

	@Test
	void controllerRejectsUiUpdatesAwayFromTheEdt() {
		QuizSession session = sessionFromFakeRepository();
		FakeQuizView view = new FakeQuizView();
		QuizController controller = new QuizController(session, view, "Student");

		assertThrows(IllegalStateException.class, controller::start);
		assertThrows(IllegalStateException.class, controller::submitCurrentAnswer);
		assertEquals(0, view.questionDisplayCount);
	}

	private QuizSession sessionFromFakeRepository() {
		return new QuizSession(() -> List.of(question(1), question(2), question(3), question(4), question(5)),
				new Random(19L));
	}

	private Question question(int id) {
		return new Question(id, "Question " + id, Question.DisplayType.FOUR_BUTTONS, "Answer " + id,
				List.of("Answer " + id, "Second", "Third", "Fourth"));
	}

	private void onEdt(Runnable action) throws Exception {
		SwingUtilities.invokeAndWait(action);
	}

	private static final class FakeQuizView implements QuizView {

		private Question currentQuestion;
		private String selectedAnswer = "";
		private String validationMessage;
		private String reportStudentName;
		private QuizReport report;
		private int questionDisplayCount;
		private int validationErrorCount;
		private int reportCount;
		private boolean submissionEnabled;
		private boolean answerCleared;
		private boolean allCallsOnEdt = true;

		@Override
		public void showQuestion(Question question) {
			recordThread();
			currentQuestion = question;
			selectedAnswer = "";
			questionDisplayCount++;
		}

		@Override
		public String getSelectedAnswer() {
			recordThread();
			return selectedAnswer;
		}

		@Override
		public void clearAnswer() {
			recordThread();
			selectedAnswer = "";
			answerCleared = true;
		}

		@Override
		public void showValidationError(String message) {
			recordThread();
			validationMessage = message;
			validationErrorCount++;
		}

		@Override
		public void showAnswerFeedback(boolean correct) {
			recordThread();
		}

		@Override
		public void updateScore(int score) {
			recordThread();
		}

		@Override
		public void setSubmissionEnabled(boolean enabled) {
			recordThread();
			submissionEnabled = enabled;
		}

		@Override
		public void showReport(QuizReport report, String studentName) {
			recordThread();
			this.report = report;
			reportStudentName = studentName;
			reportCount++;
			assertNotNull(report);
		}

		private void recordThread() {
			allCallsOnEdt &= SwingUtilities.isEventDispatchThread();
		}
	}
}
