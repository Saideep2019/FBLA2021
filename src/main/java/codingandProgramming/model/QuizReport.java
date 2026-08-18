package codingandProgramming.model;

import java.util.List;

/**
 * A read-only report of the answers submitted during a quiz session.
 */
public final class QuizReport {

	private final List<QuizResult> rows;
	private final int totalQuestionCount;

	QuizReport(List<QuizResult> rows, int totalQuestionCount) {
		this.rows = List.copyOf(rows);
		this.totalQuestionCount = totalQuestionCount;
	}

	public List<QuizResult> getRows() {
		return rows;
	}

	public int getQuestionsAttempted() {
		return rows.size();
	}

	public int getCorrectAnswerCount() {
		return (int) rows.stream().filter(QuizResult::isCorrect).count();
	}

	public int getPercentageCorrect() {
		return getCorrectAnswerCount() * 100 / totalQuestionCount;
	}
}
