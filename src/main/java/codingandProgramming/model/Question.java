package codingandProgramming.model;

import java.util.List;
import java.util.Objects;

/**
 * An immutable quiz question and the information needed to display it.
 */
public final class Question {

	public enum DisplayType {
		FOUR_BUTTONS(1),
		DROP_DOWN(2),
		TRUE_FALSE(3),
		FILL_IN_THE_BLANK(4);

		private final int legacyValue;

		DisplayType(int legacyValue) {
			this.legacyValue = legacyValue;
		}

		public int getLegacyValue() {
			return legacyValue;
		}

		public static DisplayType fromLegacyValue(int legacyValue) {
			for (DisplayType displayType : values()) {
				if (displayType.legacyValue == legacyValue) {
					return displayType;
				}
			}
			throw new IllegalArgumentException("Unknown display type: " + legacyValue);
		}
	}

	private final int id;
	private final String text;
	private final DisplayType displayType;
	private final String correctAnswer;
	private final List<String> answerOptions;

	public Question(int id, String text, DisplayType displayType, String correctAnswer,
			List<String> answerOptions) {
		this.id = id;
		this.text = Objects.requireNonNull(text, "text");
		this.displayType = Objects.requireNonNull(displayType, "displayType");
		this.correctAnswer = Objects.requireNonNull(correctAnswer, "correctAnswer");
		this.answerOptions = List.copyOf(answerOptions);
	}

	public int getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public DisplayType getDisplayType() {
		return displayType;
	}

	public String getCorrectAnswer() {
		return correctAnswer;
	}

	public List<String> getAnswerOptions() {
		return answerOptions;
	}

	public String getFirstBlankFragment() {
		return answerOptions.get(0);
	}

	public String getSecondBlankFragment() {
		return answerOptions.get(1);
	}

	public String getReportQuestion() {
		if (displayType == DisplayType.FILL_IN_THE_BLANK) {
			return getFirstBlankFragment() + " ____ " + getSecondBlankFragment();
		}
		return text;
	}
}
