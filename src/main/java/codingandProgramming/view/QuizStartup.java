package codingandProgramming.view;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import codingandProgramming.model.QuestionRepository;
import codingandProgramming.model.QuizSession;

/**
 * Loads a quiz session in the background and reports its result on the EDT.
 */
final class QuizStartup {

	private final QuestionRepository repository;
	private final Random random;
	private final Consumer<QuizSession> initializeQuizDisplay;
	private final Runnable presentUnavailableError;
	private boolean attempted;
	private boolean successful;
	private SwingWorker<QuizSession, Void> worker;

	QuizStartup(QuestionRepository repository, Random random, Consumer<QuizSession> initializeQuizDisplay,
			Runnable presentUnavailableError) {
		this.repository = Objects.requireNonNull(repository, "repository");
		this.random = Objects.requireNonNull(random, "random");
		this.initializeQuizDisplay = Objects.requireNonNull(initializeQuizDisplay, "initializeQuizDisplay");
		this.presentUnavailableError = Objects.requireNonNull(presentUnavailableError, "presentUnavailableError");
	}

	void start() {
		SwingThreading.requireEventDispatchThread();
		if (attempted) {
			return;
		}
		attempted = true;

		worker = new SwingWorker<>() {
			@Override
			protected QuizSession doInBackground() {
				return new QuizSession(repository, random);
			}

			@Override
			protected void done() {
				SwingThreading.requireEventDispatchThread();
				try {
					QuizSession session = get();
					initializeQuizDisplay.accept(session);
					successful = true;
				} catch (CancellationException ignored) {
					// Application shutdown deliberately suppresses startup UI.
				} catch (InterruptedException interrupted) {
					Thread.currentThread().interrupt();
					presentUnavailableError.run();
				} catch (ExecutionException | RuntimeException initializationFailure) {
					presentUnavailableError.run();
				}
			}
		};
		worker.execute();
	}

	boolean wasSuccessful() {
		return successful;
	}

	void cancel() {
		if (worker != null) {
			worker.cancel(true);
		}
	}
}
