package codingandProgramming.view;

import java.util.Objects;

import codingandProgramming.model.QuizResult;
import codingandProgramming.model.QuizSession;

/**
 * Coordinates user submissions between a quiz session and its view.
 */
public final class QuizController {

	private final QuizSession session;
	private final QuizView view;
	private final String studentName;
	private boolean started;
	private boolean reportDisplayed;

	QuizController(QuizSession session, QuizView view, String studentName) {
		this.session = Objects.requireNonNull(session, "session");
		this.view = Objects.requireNonNull(view, "view");
		this.studentName = Objects.requireNonNullElse(studentName, "");
	}

	void start() {
		SwingThreading.requireEventDispatchThread();
		if (started) {
			return;
		}
		started = true;
		view.updateScore(session.getScore());
		view.setSubmissionEnabled(true);
		view.showQuestion(session.getCurrentQuestion());
	}

	public void submitCurrentAnswer() {
		SwingThreading.requireEventDispatchThread();
		if (!started || session.isComplete()) {
			view.setSubmissionEnabled(false);
			return;
		}

		QuizResult result;
		try {
			result = session.submitAnswer(view.getSelectedAnswer());
		} catch (IllegalArgumentException validationError) {
			view.showValidationError(validationError.getMessage());
			return;
		}

		view.showAnswerFeedback(result.isCorrect());
		view.updateScore(session.getScore());

		if (session.isComplete()) {
			view.clearAnswer();
			view.setSubmissionEnabled(false);
			if (!reportDisplayed) {
				reportDisplayed = true;
				view.showReport(session.getReport(), studentName);
			}
			return;
		}

		view.showQuestion(session.getCurrentQuestion());
	}
}
