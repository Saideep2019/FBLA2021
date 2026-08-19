package codingandProgramming.model;

/**
 * Reports quiz-content failures without leaking database implementation details.
 */
public final class QuestionRepositoryException extends IllegalStateException {

	public static final String USER_MESSAGE =
			"Quiz questions could not be loaded. Please verify that the quiz data is available and try again.";

	QuestionRepositoryException() {
		super(USER_MESSAGE);
	}

	QuestionRepositoryException(Throwable cause) {
		super(USER_MESSAGE, cause);
	}
}
