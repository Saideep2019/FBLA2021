package codingandProgramming.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.stream.IntStream;

import org.h2.tools.RunScript;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class quizDAOTest {

	@Test
	void daoLoadsAllActualQuestionIdsWithoutChangingThem(@TempDir Path tempDirectory) throws Exception {
		String databaseUrl = databaseUrl(tempDirectory, "complete-quiz");
		try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "");
				Reader script = Files.newBufferedReader(Path.of("database", "quiz-content.sql"))) {
			RunScript.execute(connection, script);
		}

		try (quizDAO dao = new quizDAO(databaseUrl)) {
			List<Question> questions = dao.findAllQuestions();

			assertEquals(50, questions.size());
			assertEquals(IntStream.rangeClosed(1, 50).boxed().toList(),
					questions.stream().map(Question::getId).toList());
			assertEquals(49, new QuestionAndOptionsModel(questionWithId(questions, 49)).getQuestionId());
			assertEquals(50, new QuestionAndOptionsModel(questionWithId(questions, 50)).getQuestionId());
		}
	}

	@Test
	void missingDatabaseTablesProduceAnUnderstandableRepositoryError(@TempDir Path tempDirectory) throws Exception {
		String databaseUrl = databaseUrl(tempDirectory, "missing-tables");
		try (Connection ignored = DriverManager.getConnection(databaseUrl, "sa", "")) {
			// Create only the disposable database file; the expected quiz tables are absent.
		}

		try (quizDAO dao = new quizDAO(databaseUrl)) {
			QuestionRepositoryException exception = assertThrows(QuestionRepositoryException.class,
					dao::findAllQuestions);

			assertEquals(QuestionRepositoryException.USER_MESSAGE, exception.getMessage());
			assertFalse(exception.getMessage().contains("SELECT"));
		}
	}

	@Test
	void incompleteQuestionRecordsProduceAnUnderstandableRepositoryError(@TempDir Path tempDirectory)
			throws Exception {
		String databaseUrl = databaseUrl(tempDirectory, "incomplete-question");
		try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "");
				Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE questions (question VARCHAR(255), questionid INTEGER, "
					+ "displaytype INTEGER, answer VARCHAR(255))");
			statement.execute("CREATE TABLE answers (answers VARCHAR(255), questionid INTEGER, id INTEGER)");
			for (int id = 1; id <= 4; id++) {
				statement.execute("INSERT INTO questions VALUES ('Question " + id + "', " + id + ", 3, 'true')");
			}
			statement.execute("INSERT INTO questions VALUES ('Fill in the blanks', 5, 4, 'missing')");
		}

		try (quizDAO dao = new quizDAO(databaseUrl)) {
			QuestionRepositoryException exception = assertThrows(QuestionRepositoryException.class,
					dao::findAllQuestions);

			assertEquals(QuestionRepositoryException.USER_MESSAGE, exception.getMessage());
		}
	}

	private String databaseUrl(Path tempDirectory, String databaseName) {
		return "jdbc:h2:file:" + tempDirectory.resolve(databaseName).toAbsolutePath().toString().replace('\\', '/');
	}

	private Question questionWithId(List<Question> questions, int id) {
		return questions.stream().filter(question -> question.getId() == id).findFirst().orElseThrow();
	}
}
