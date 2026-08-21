package codingandProgramming.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import codingandProgramming.model.Question;
import codingandProgramming.model.QuestionRepository;
import codingandProgramming.model.QuizReport;

class QuizApplicationTest {

	@Test
	void fastInitializationKeepsLoadingVisibleUntilTheMinimumDuration() throws Exception {
		List<String> startupEvents = Collections.synchronizedList(new ArrayList<>());
		FakeNanoClock clock = new FakeNanoClock();
		FakeDelayScheduler scheduler = new FakeDelayScheduler();
		AtomicBoolean repositoryCalledOnEdt = new AtomicBoolean(true);
		QuestionRepository repository = () -> {
			repositoryCalledOnEdt.set(SwingUtilities.isEventDispatchThread());
			startupEvents.add("background initialization completed");
			return questions();
		};
		AtomicInteger closeCount = new AtomicInteger();
		FakeApplicationUi ui = new FakeApplicationUi(startupEvents);
		QuizApplication application = new QuizApplication(repository, new Random(9L), ui,
				closeCount::incrementAndGet, clock, scheduler);

		onEdt(() -> {
			application.start();
			application.start();
		});
		await(scheduler.scheduled);

		assertEquals(2_500, QuizApplication.MINIMUM_SPLASH_MILLIS);
		assertEquals(QuizApplication.MINIMUM_SPLASH_MILLIS, scheduler.delayMillis);
		assertEquals(1, scheduler.scheduleCount);
		assertEquals(0, ui.hideLoadingCount);
		assertEquals(0, ui.nameRequestCount);
		assertEquals(0, ui.showWindowCount);
		assertEquals(List.of("loading shown", "background initialization completed"), startupEvents);

		clock.advanceMillis(scheduler.delayMillis);
		onEdt(scheduler::runScheduled);
		await(ui.finished);

		assertSuccessfulStartup(ui, repositoryCalledOnEdt, scheduler);
		assertEquals(List.of("loading shown", "background initialization completed", "loading hidden/disposed",
				"name requested", "quiz displayed"), startupEvents);
		onEdt(application::shutdown);
		assertEquals(1, closeCount.get());
	}

	@Test
	void slowInitializationContinuesImmediatelyWithoutSchedulingAnotherDelay() throws Exception {
		List<String> startupEvents = Collections.synchronizedList(new ArrayList<>());
		FakeNanoClock clock = new FakeNanoClock();
		FakeDelayScheduler scheduler = new FakeDelayScheduler();
		AtomicBoolean repositoryCalledOnEdt = new AtomicBoolean(true);
		QuestionRepository repository = () -> {
			repositoryCalledOnEdt.set(SwingUtilities.isEventDispatchThread());
			clock.advanceMillis(QuizApplication.MINIMUM_SPLASH_MILLIS + 500L);
			startupEvents.add("background initialization completed");
			return questions();
		};
		AtomicInteger closeCount = new AtomicInteger();
		FakeApplicationUi ui = new FakeApplicationUi(startupEvents);
		QuizApplication application = new QuizApplication(repository, new Random(10L), ui,
				closeCount::incrementAndGet, clock, scheduler);

		onEdt(application::start);
		await(ui.finished);

		assertEquals(0, scheduler.scheduleCount);
		assertSuccessfulStartup(ui, repositoryCalledOnEdt, scheduler);
		assertEquals(List.of("loading shown", "background initialization completed", "loading hidden/disposed",
				"name requested", "quiz displayed"), startupEvents);
		onEdt(application::shutdown);
		assertEquals(1, closeCount.get());
	}

	@Test
	void startupFailureClosesLoadingBeforeUnavailableWithoutPromptOrQuiz() throws Exception {
		List<String> startupEvents = Collections.synchronizedList(new ArrayList<>());
		FakeNanoClock clock = new FakeNanoClock();
		FakeDelayScheduler scheduler = new FakeDelayScheduler();
		AtomicBoolean repositoryCalledOnEdt = new AtomicBoolean(true);
		QuestionRepository repository = () -> {
			repositoryCalledOnEdt.set(SwingUtilities.isEventDispatchThread());
			startupEvents.add("background initialization failed");
			throw new IllegalStateException("private startup detail");
		};
		AtomicInteger closeCount = new AtomicInteger();
		FakeApplicationUi ui = new FakeApplicationUi(startupEvents);
		QuizApplication application = new QuizApplication(repository, new Random(8L), ui,
				closeCount::incrementAndGet, clock, scheduler);

		onEdt(application::start);
		await(scheduler.scheduled);
		assertEquals(0, ui.hideLoadingCount);
		assertEquals(0, ui.unavailableCount);

		clock.advanceMillis(scheduler.delayMillis);
		onEdt(scheduler::runScheduled);
		await(ui.finished);

		assertEquals(1, ui.loadingCount);
		assertEquals(1, ui.hideLoadingCount);
		assertEquals(1, ui.unavailableCount);
		assertEquals(0, ui.nameRequestCount);
		assertEquals(0, ui.createWindowCount);
		assertEquals(0, ui.showWindowCount);
		assertEquals(1, closeCount.get());
		assertFalse(repositoryCalledOnEdt.get());
		assertTrue(ui.allCallsOnEdt);
		assertTrue(scheduler.allCallsOnEdt);
		assertEquals(List.of("loading shown", "background initialization failed", "loading hidden/disposed",
				"unavailable shown"), startupEvents);
	}

	private void assertSuccessfulStartup(FakeApplicationUi ui, AtomicBoolean repositoryCalledOnEdt,
			FakeDelayScheduler scheduler) {
		assertEquals(1, ui.loadingCount);
		assertEquals(1, ui.hideLoadingCount);
		assertEquals(0, ui.unavailableCount);
		assertEquals(1, ui.nameRequestCount);
		assertEquals(1, ui.createWindowCount);
		assertEquals(1, ui.showWindowCount);
		assertEquals(1, ui.window.questionDisplayCount);
		assertTrue(ui.window.submissionEnabled);
		assertNotNull(ui.window.submitHandler);
		assertNotNull(ui.window.closeHandler);
		assertFalse(repositoryCalledOnEdt.get());
		assertTrue(ui.allCallsOnEdt);
		assertTrue(ui.window.allCallsOnEdt);
		assertTrue(scheduler.allCallsOnEdt);
	}

	private void onEdt(Runnable action) throws Exception {
		SwingUtilities.invokeAndWait(action);
	}

	private void await(CountDownLatch latch) throws InterruptedException {
		assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for application startup");
	}

	private static List<Question> questions() {
		return List.of(question(1), question(2), question(3), question(4), question(5));
	}

	private static Question question(int id) {
		return new Question(id, "Question " + id, Question.DisplayType.FOUR_BUTTONS, "Answer " + id,
				List.of("Answer " + id, "Second", "Third", "Fourth"));
	}

	private static final class FakeNanoClock implements LongSupplier {

		private final AtomicLong currentNanos = new AtomicLong();

		@Override
		public long getAsLong() {
			return currentNanos.get();
		}

		private void advanceMillis(long milliseconds) {
			currentNanos.addAndGet(TimeUnit.MILLISECONDS.toNanos(milliseconds));
		}
	}

	private static final class FakeDelayScheduler implements SplashDelayScheduler {

		private final CountDownLatch scheduled = new CountDownLatch(1);
		private int delayMillis;
		private int scheduleCount;
		private Runnable continuation;
		private boolean allCallsOnEdt = true;

		@Override
		public void schedule(int delayMillis, Runnable continuation) {
			recordThread();
			this.delayMillis = delayMillis;
			this.continuation = continuation;
			scheduleCount++;
			scheduled.countDown();
		}

		private void runScheduled() {
			recordThread();
			Runnable scheduledContinuation = continuation;
			continuation = null;
			scheduledContinuation.run();
		}

		private void recordThread() {
			allCallsOnEdt &= SwingUtilities.isEventDispatchThread();
		}
	}

	private static final class FakeApplicationUi implements QuizApplicationUi {

		private final CountDownLatch finished = new CountDownLatch(1);
		private final FakeQuizWindow window = new FakeQuizWindow();
		private final List<String> startupEvents;
		private int loadingCount;
		private int hideLoadingCount;
		private int unavailableCount;
		private int nameRequestCount;
		private int createWindowCount;
		private int showWindowCount;
		private boolean allCallsOnEdt = true;

		private FakeApplicationUi(List<String> startupEvents) {
			this.startupEvents = startupEvents;
		}

		@Override
		public void showLoading() {
			recordThread();
			loadingCount++;
			startupEvents.add("loading shown");
		}

		@Override
		public void hideLoading() {
			recordThread();
			hideLoadingCount++;
			startupEvents.add("loading hidden/disposed");
		}

		@Override
		public String requestStudentName() {
			recordThread();
			nameRequestCount++;
			startupEvents.add("name requested");
			return "Test Student";
		}

		@Override
		public QuizWindow createQuizWindow(String studentName) {
			recordThread();
			assertEquals("Test Student", studentName);
			createWindowCount++;
			return window;
		}

		@Override
		public void showQuizWindow(QuizWindow window) {
			recordThread();
			showWindowCount++;
			window.showWindow();
			startupEvents.add("quiz displayed");
			finished.countDown();
		}

		@Override
		public void showUnavailable() {
			recordThread();
			unavailableCount++;
			startupEvents.add("unavailable shown");
			finished.countDown();
		}

		private void recordThread() {
			allCallsOnEdt &= SwingUtilities.isEventDispatchThread();
		}
	}

	private static final class FakeQuizWindow implements QuizWindow {

		private Runnable submitHandler;
		private Runnable closeHandler;
		private int questionDisplayCount;
		private boolean submissionEnabled;
		private boolean allCallsOnEdt = true;

		@Override
		public void setSubmitHandler(Runnable submitHandler) {
			recordThread();
			this.submitHandler = submitHandler;
		}

		@Override
		public void setCloseHandler(Runnable closeHandler) {
			recordThread();
			this.closeHandler = closeHandler;
		}

		@Override
		public void showWindow() {
			recordThread();
		}

		@Override
		public void disposeWindow() {
			recordThread();
		}

		@Override
		public void showQuestion(Question question) {
			recordThread();
			questionDisplayCount++;
		}

		@Override
		public String getSelectedAnswer() {
			recordThread();
			return "";
		}

		@Override
		public void clearAnswer() {
			recordThread();
		}

		@Override
		public void showValidationError(String message) {
			recordThread();
		}

		@Override
		public void showAnswerFeedback(boolean correct) {
			recordThread();
		}

		@Override
		public void updateScore(int score) {
			recordThread();
		}

		@Override
		public void setSubmissionEnabled(boolean enabled) {
			recordThread();
			submissionEnabled = enabled;
		}

		@Override
		public void showReport(QuizReport report, String studentName) {
			recordThread();
		}

		private void recordThread() {
			allCallsOnEdt &= SwingUtilities.isEventDispatchThread();
		}
	}
}
