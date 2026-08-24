package codingandProgramming.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import codingandProgramming.model.Question;
import codingandProgramming.model.QuestionRepository;
import codingandProgramming.model.QuizSession;
import codingandProgramming.model.quizDAO;

class QuizStartupTest {

	@Test
	void repositoryFailurePresentsOneErrorAndDoesNotContinueToNormalQuizDisplay() throws Exception {
		AtomicBoolean repositoryCalledOnEdt = new AtomicBoolean(true);
		QuestionRepository failingRepository = () -> {
			repositoryCalledOnEdt.set(SwingUtilities.isEventDispatchThread());
			throw new IllegalStateException("internal database detail");
		};
		AtomicReference<QuizSession> displayedSession = new AtomicReference<>();
		AtomicInteger errorCount = new AtomicInteger();
		AtomicBoolean callbackOnEdt = new AtomicBoolean();
		CountDownLatch finished = new CountDownLatch(1);
		QuizStartup startup = new QuizStartup(failingRepository, new Random(1L), session -> {
			displayedSession.set(session);
			finished.countDown();
		}, () -> {
			callbackOnEdt.set(SwingUtilities.isEventDispatchThread());
			errorCount.incrementAndGet();
			finished.countDown();
		});

		onEdt(() -> {
			startup.start();
			startup.start();
		});
		await(finished);

		assertNull(displayedSession.get());
		assertEquals(1, errorCount.get());
		assertFalse(repositoryCalledOnEdt.get());
		assertTrue(callbackOnEdt.get());
		assertFalse(startup.wasSuccessful());
	}

	@Test
	void insufficientQuestionDataPreventsStartup() throws Exception {
		assertStartupFails(() -> List.of(question(1), question(2), question(3), question(4)));
	}

	@Test
	void malformedQuestionDataPreventsStartup() throws Exception {
		QuestionRepository malformedRepository = () -> {
			List<Question> questions = new ArrayList<>();
			questions.add(question(1));
			questions.add(question(2));
			questions.add(null);
			questions.add(question(4));
			questions.add(question(5));
			return questions;
		};

		assertStartupFails(malformedRepository);
	}

	@Test
	void validQuestionDataInitializesTheNormalQuizDisplayOnceOnTheEdt() throws Exception {
		AtomicBoolean repositoryCalledOnEdt = new AtomicBoolean(true);
		QuestionRepository validRepository = () -> {
			repositoryCalledOnEdt.set(SwingUtilities.isEventDispatchThread());
			return List.of(question(1), question(2), question(3), question(4), question(5));
		};
		AtomicReference<QuizSession> displayedSession = new AtomicReference<>();
		AtomicInteger displayCount = new AtomicInteger();
		AtomicInteger errorCount = new AtomicInteger();
		AtomicBoolean displayOnEdt = new AtomicBoolean();
		CountDownLatch finished = new CountDownLatch(1);
		QuizStartup startup = new QuizStartup(validRepository, new Random(2L), session -> {
			displayedSession.set(session);
			displayOnEdt.set(SwingUtilities.isEventDispatchThread());
			displayCount.incrementAndGet();
			finished.countDown();
		}, () -> {
			errorCount.incrementAndGet();
			finished.countDown();
		});

		onEdt(() -> {
			startup.start();
			startup.start();
		});
		await(finished);

		assertNotNull(displayedSession.get());
		assertEquals(5, displayedSession.get().getSelectedQuestions().size());
		assertEquals(1, displayCount.get());
		assertEquals(0, errorCount.get());
		assertFalse(repositoryCalledOnEdt.get());
		assertTrue(displayOnEdt.get());
		assertTrue(startup.wasSuccessful());
	}

	@Test
	void databaseInitializationFailureUsesTheSafeUnavailableStartup(@TempDir Path tempDirectory) throws Exception {
		Path blockedParent = tempDirectory.resolve("not-a-directory");
		Files.writeString(blockedParent, "blocks database directory creation");
		AtomicReference<QuizSession> displayedSession = new AtomicReference<>();
		AtomicInteger errorCount = new AtomicInteger();
		CountDownLatch finished = new CountDownLatch(1);

		try (quizDAO dao = new quizDAO(blockedParent.resolve("quizdb"))) {
			QuizStartup startup = new QuizStartup(dao, new Random(4L), displayedSession::set, () -> {
				errorCount.incrementAndGet();
				finished.countDown();
			});

			onEdt(() -> {
				startup.start();
				startup.start();
			});
			await(finished);
		}

		assertNull(displayedSession.get());
		assertEquals(1, errorCount.get());
	}

	private void assertStartupFails(QuestionRepository repository) throws Exception {
		AtomicReference<QuizSession> displayedSession = new AtomicReference<>();
		AtomicInteger errorCount = new AtomicInteger();
		CountDownLatch finished = new CountDownLatch(1);
		QuizStartup startup = new QuizStartup(repository, new Random(3L), displayedSession::set, () -> {
			errorCount.incrementAndGet();
			finished.countDown();
		});

		onEdt(startup::start);
		await(finished);

		assertNull(displayedSession.get());
		assertEquals(1, errorCount.get());
	}

	private void onEdt(Runnable action) throws Exception {
		SwingUtilities.invokeAndWait(action);
	}

	private void await(CountDownLatch latch) throws InterruptedException {
		assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for Swing startup");
	}

	private Question question(int id) {
		return new Question(id, "Question " + id, Question.DisplayType.FOUR_BUTTONS, "Answer " + id,
				List.of("Answer " + id, "Second", "Third", "Fourth"));
	}
}
