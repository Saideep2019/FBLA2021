package codingandProgramming.view;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import codingandProgramming.model.QuestionRepository;
import codingandProgramming.model.QuizSession;

/**
 * Gates normal quiz startup on successful session initialization.
 */
final class QuizStartup {

	private final QuestionRepository repository;
	private final Random random;
	private final Consumer<QuizSession> initializeQuizDisplay;
	private final Runnable presentUnavailableError;
	private boolean attempted;
	private boolean successful;

	QuizStartup(QuestionRepository repository, Random random, Consumer<QuizSession> initializeQuizDisplay,
			Runnable presentUnavailableError) {
		this.repository = Objects.requireNonNull(repository, "repository");
		this.random = Objects.requireNonNull(random, "random");
		this.initializeQuizDisplay = Objects.requireNonNull(initializeQuizDisplay, "initializeQuizDisplay");
		this.presentUnavailableError = Objects.requireNonNull(presentUnavailableError, "presentUnavailableError");
	}

	boolean start() {
		if (attempted) {
			return successful;
		}
		attempted = true;

		try {
			QuizSession session = new QuizSession(repository, random);
			initializeQuizDisplay.accept(session);
			successful = true;
		} catch (RuntimeException initializationFailure) {
			presentUnavailableError.run();
		}
		return successful;
	}
}
