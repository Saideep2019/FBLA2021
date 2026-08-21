package codingandProgramming.view;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import codingandProgramming.model.QuestionRepository;
import codingandProgramming.model.QuizSession;
import codingandProgramming.model.quizDAO;

/**
 * Application entry point and owner of startup and repository lifecycle.
 */
public final class QuizApplication {

	static final int MINIMUM_SPLASH_MILLIS = 2_500;
	private static final long NANOS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1);
	private static final long MINIMUM_SPLASH_NANOS = TimeUnit.MILLISECONDS.toNanos(MINIMUM_SPLASH_MILLIS);

	private final QuestionRepository repository;
	private final Random random;
	private final QuizApplicationUi applicationUi;
	private final Runnable resourceCloser;
	private final LongSupplier nanoTime;
	private final SplashDelayScheduler delayScheduler;
	private volatile QuizStartup startup;
	private QuizWindow quizWindow;
	private long loadingShownAtNanos;
	private boolean started;
	private boolean closed;

	QuizApplication(QuestionRepository repository, Random random, QuizApplicationUi applicationUi,
			Runnable resourceCloser) {
		this(repository, random, applicationUi, resourceCloser, System::nanoTime,
				QuizApplication::scheduleWithSwingTimer);
	}

	QuizApplication(QuestionRepository repository, Random random, QuizApplicationUi applicationUi,
			Runnable resourceCloser, LongSupplier nanoTime, SplashDelayScheduler delayScheduler) {
		this.repository = Objects.requireNonNull(repository, "repository");
		this.random = Objects.requireNonNull(random, "random");
		this.applicationUi = Objects.requireNonNull(applicationUi, "applicationUi");
		this.resourceCloser = Objects.requireNonNull(resourceCloser, "resourceCloser");
		this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
		this.delayScheduler = Objects.requireNonNull(delayScheduler, "delayScheduler");
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			quizDAO repository = new quizDAO();
			QuizApplication application = new QuizApplication(repository, new Random(),
					new SwingQuizApplicationUi(), repository::close);
			Runtime.getRuntime().addShutdownHook(new Thread(application::shutdown, "quiz-resource-cleanup"));
			application.start();
		});
	}

	void start() {
		SwingThreading.requireEventDispatchThread();
		if (started) {
			return;
		}
		started = true;
		applicationUi.showLoading();
		loadingShownAtNanos = nanoTime.getAsLong();
		startup = new QuizStartup(repository, random, this::finishStartup, this::failStartup);
		startup.start();
	}

	private void finishStartup(QuizSession session) {
		SwingThreading.requireEventDispatchThread();
		if (closed) {
			return;
		}
		continueAfterMinimumSplash(() -> completeSuccessfulStartup(session));
	}

	private void completeSuccessfulStartup(QuizSession session) {
		SwingThreading.requireEventDispatchThread();
		if (closed) {
			return;
		}
		applicationUi.hideLoading();
		String studentName = Objects.requireNonNullElse(applicationUi.requestStudentName(), "");
		QuizWindow window = applicationUi.createQuizWindow(studentName);
		quizWindow = window;
		QuizController controller = new QuizController(session, window, studentName);
		window.setSubmitHandler(controller::submitCurrentAnswer);
		window.setCloseHandler(this::shutdown);
		controller.start();
		applicationUi.showQuizWindow(window);
	}

	private void failStartup() {
		SwingThreading.requireEventDispatchThread();
		if (closed) {
			return;
		}
		continueAfterMinimumSplash(this::completeFailedStartup);
	}

	private void completeFailedStartup() {
		SwingThreading.requireEventDispatchThread();
		if (closed) {
			return;
		}
		applicationUi.hideLoading();
		if (quizWindow != null) {
			quizWindow.disposeWindow();
			quizWindow = null;
		}
		shutdown();
		applicationUi.showUnavailable();
	}

	private void continueAfterMinimumSplash(Runnable continuation) {
		SwingThreading.requireEventDispatchThread();
		long elapsedNanos = Math.max(0, nanoTime.getAsLong() - loadingShownAtNanos);
		long remainingNanos = MINIMUM_SPLASH_NANOS - elapsedNanos;
		if (remainingNanos <= 0) {
			continuation.run();
			return;
		}

		long roundedUpMillis = (remainingNanos + NANOS_PER_MILLISECOND - 1) / NANOS_PER_MILLISECOND;
		delayScheduler.schedule((int) roundedUpMillis, () -> continueAfterMinimumSplash(continuation));
	}

	private static void scheduleWithSwingTimer(int delayMillis, Runnable continuation) {
		SwingThreading.requireEventDispatchThread();
		Timer timer = new Timer(delayMillis, event -> continuation.run());
		timer.setRepeats(false);
		timer.start();
	}

	synchronized void shutdown() {
		if (closed) {
			return;
		}
		closed = true;
		QuizStartup currentStartup = startup;
		if (currentStartup != null) {
			currentStartup.cancel();
		}
		try {
			resourceCloser.run();
		} catch (RuntimeException ignored) {
			// A startup error dialog has already supplied the only user-facing failure.
		}
	}
}

@FunctionalInterface
interface SplashDelayScheduler {

	void schedule(int delayMillis, Runnable continuation);
}
