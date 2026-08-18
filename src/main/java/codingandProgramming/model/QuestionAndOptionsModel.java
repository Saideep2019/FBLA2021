package codingandProgramming.model;

import java.util.List;
import java.util.Objects;

/**
 * Legacy Swing adapter for the extracted {@link Question} domain object.
 */
public final class QuestionAndOptionsModel {

	private final Question question;

	public QuestionAndOptionsModel(Question question) {
		this.question = Objects.requireNonNull(question, "question");
	}

	public String getQuestionParttwo() {
		if (question.getDisplayType() == Question.DisplayType.FILL_IN_THE_BLANK) {
			return question.getSecondBlankFragment();
		}
		return null;
	}

	public int getQuestionId() {
		return question.getId();
	}

	public String getRightAnswer() {
		return question.getCorrectAnswer();
	}

	public int getDisplayType() {
		return question.getDisplayType().getLegacyValue();
	}

	public List<String> getOptions() {
		return question.getAnswerOptions();
	}

	public String getQuestion() {
		return question.getText();
	}

	public Question toQuestion() {
		return question;
	}

	@Override
	public String toString() {
		return getQuestion() + " ---" + getOptions();
	}
}
