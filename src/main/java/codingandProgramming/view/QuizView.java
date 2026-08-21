package codingandProgramming.view;

import codingandProgramming.model.Question;
import codingandProgramming.model.QuizReport;

/**
 * The controller-facing operations of the quiz window.
 */
interface QuizView {

	void showQuestion(Question question);

	String getSelectedAnswer();

	void clearAnswer();

	void showValidationError(String message);

	void showAnswerFeedback(boolean correct);

	void updateScore(int score);

	void setSubmissionEnabled(boolean enabled);

	void showReport(QuizReport report, String studentName);
}
