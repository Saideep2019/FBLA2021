package codingandProgramming.model;

import java.util.Objects;

/**
 * The recorded answer and outcome for one question.
 */
public final class QuizResult {

	private final Question question;
	private final String selectedAnswer;
	private final boolean correct;

	public QuizResult(Question question, String selectedAnswer, boolean correct) {
		this.question = Objects.requireNonNull(question, "question");
		this.selectedAnswer = Objects.requireNonNull(selectedAnswer, "selectedAnswer");
		this.correct = correct;
	}

	public Question getQuestion() {
		return question;
	}

	public String getQuestionText() {
		return question.getReportQuestion();
	}

	public String getCorrectAnswer() {
		return question.getCorrectAnswer();
	}

	public String getSelectedAnswer() {
		return selectedAnswer;
	}

	public boolean isCorrect() {
		return correct;
	}
}
