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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import codingandProgramming.model.Question;
import codingandProgramming.model.QuestionRepository;
import codingandProgramming.model.QuizSession;
import codingandProgramming.model.quizDAO;

class QuizStartupTest {

	@Test
	void repositoryFailurePresentsOneErrorAndDoesNotContinueToNormalQuizDisplay() {
		QuestionRepository failingRepository = () -> {
			throw new IllegalStateException("internal database detail");
		};
		AtomicReference<QuizSession> displayedSession = new AtomicReference<>();
		AtomicInteger errorCount = new AtomicInteger();
		QuizStartup startup = new QuizStartup(failingRepository, new Random(1L), displayedSession::set,
				errorCount::incrementAndGet);

		assertFalse(startup.start());
		assertFalse(startup.start());

		assertNull(displayedSession.get());
		assertEquals(1, errorCount.get());
	}

	@Test
	void insufficientQuestionDataPreventsStartup() {
		assertStartupFails(() -> List.of(question(1), question(2), question(3), question(4)));
	}

	@Test
	void malformedQuestionDataPreventsStartup() {
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
	void validQuestionDataInitializesTheNormalQuizDisplayOnce() {
		QuestionRepository validRepository = () -> List.of(question(1), question(2), question(3), question(4),
				question(5));
		AtomicReference<QuizSession> displayedSession = new AtomicReference<>();
		AtomicInteger displayCount = new AtomicInteger();
		AtomicInteger errorCount = new AtomicInteger();
		QuizStartup startup = new QuizStartup(validRepository, new Random(2L), session -> {
			displayedSession.set(session);
			displayCount.incrementAndGet();
		}, errorCount::incrementAndGet);

		assertTrue(startup.start());
		assertTrue(startup.start());

		assertNotNull(displayedSession.get());
		assertEquals(5, displayedSession.get().getSelectedQuestions().size());
		assertEquals(1, displayCount.get());
		assertEquals(0, errorCount.get());
	}

	@Test
	void databaseInitializationFailureUsesTheSafeUnavailableStartup(@TempDir Path tempDirectory) throws Exception {
		Path blockedParent = tempDirectory.resolve("not-a-directory");
		Files.writeString(blockedParent, "blocks database directory creation");
		AtomicReference<QuizSession> displayedSession = new AtomicReference<>();
		AtomicInteger errorCount = new AtomicInteger();

		try (quizDAO dao = new quizDAO(blockedParent.resolve("quizdb"))) {
			QuizStartup startup = new QuizStartup(dao, new Random(4L), displayedSession::set,
					errorCount::incrementAndGet);

			assertFalse(startup.start());
			assertFalse(startup.start());
		}

		assertNull(displayedSession.get());
		assertEquals(1, errorCount.get());
	}

	private void assertStartupFails(QuestionRepository repository) {
		AtomicReference<QuizSession> displayedSession = new AtomicReference<>();
		AtomicInteger errorCount = new AtomicInteger();
		QuizStartup startup = new QuizStartup(repository, new Random(3L), displayedSession::set,
				errorCount::incrementAndGet);

		assertFalse(startup.start());
		assertNull(displayedSession.get());
		assertEquals(1, errorCount.get());
	}

	private Question question(int id) {
		return new Question(id, "Question " + id, Question.DisplayType.FOUR_BUTTONS, "Answer " + id,
				List.of("Answer " + id, "Second", "Third", "Fourth"));
	}
}
