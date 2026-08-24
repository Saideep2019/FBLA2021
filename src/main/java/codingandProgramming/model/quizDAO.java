/**
 * Loads quiz content from the newly initialized H2 database for the Swing application.
 */
package codingandProgramming.model;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.h2.tools.Server;

public class quizDAO implements QuestionRepository, AutoCloseable {

	private static final String DEFAULT_HOST = "jdbc:h2:file:./data/quizdb";
	private static final Path DEFAULT_DATABASE_PATH = Path.of("data", "quizdb");
	public static final String driver = "org.h2.Driver";

	String userid = "sa";
	String password = "";
	public static int studentidtochange;
	public static final int NUMBEROFQUESTIONS = QuizSession.SESSION_LENGTH;

	private final String host;
	private final DatabaseInitializer initializer;
	private final boolean startTcpServer;
	private Server dbserver;
	private Connection connection;

	public quizDAO() {
		this(DEFAULT_HOST, new DatabaseInitializer(DEFAULT_DATABASE_PATH, DEFAULT_HOST), true);
	}

	quizDAO(String host) {
		this(host, null, false);
	}

	/**
	 * Creates a repository backed by an isolated database path. Intended for tests and
	 * other explicitly configured environments.
	 */
	public quizDAO(Path databasePath) {
		this(databaseUrl(databasePath), new DatabaseInitializer(databasePath, databaseUrl(databasePath)), false);
	}

	private quizDAO(String host, DatabaseInitializer initializer, boolean startTcpServer) {
		this.host = Objects.requireNonNull(host, "host");
		this.initializer = initializer;
		this.startTcpServer = startTcpServer;
	}

	private Connection getConnection() {
		try {
			if (initializer != null) {
				initializer.initialize();
			}
			if (startTcpServer && dbserver == null) {
				dbserver = Server.createTcpServer().start();
			}

			if (connection == null) {
				Class.forName(driver);
				connection = DriverManager.getConnection(host, userid, password);
			}
			return connection;
		} catch (ClassNotFoundException | SQLException e) {
			throw new QuestionRepositoryException(e);
		}
	}

	private static String databaseUrl(Path databasePath) {
		String normalizedPath = Objects.requireNonNull(databasePath, "databasePath").toAbsolutePath().normalize()
				.toString().replace('\\', '/');
		return "jdbc:h2:file:" + normalizedPath;
	}

	@Override
	public List<Question> findAllQuestions() {
		Map<Integer, List<String>> optionsByQuestionId = loadAnswerOptions();
		List<Question> questions = new ArrayList<>();
		String sql = "SELECT question, questionid, displaytype, answer FROM questions ORDER BY questionid";

		try (Statement statement = getConnection().createStatement(); ResultSet rows = statement.executeQuery(sql)) {
			while (rows.next()) {
				int questionId = rows.getInt("questionid");
				Integer mappedQuestionId = rows.wasNull() ? null : questionId;
				questions.add(mapQuestionRecord(mappedQuestionId, rows.getString("question"),
						rows.getInt("displaytype"), rows.getString("answer"),
						optionsByQuestionId.getOrDefault(questionId, List.of())));
			}
		} catch (SQLException e) {
			throw new QuestionRepositoryException(e);
		}
		if (questions.size() < NUMBEROFQUESTIONS) {
			throw new QuestionRepositoryException();
		}
		return questions;
	}

	private Map<Integer, List<String>> loadAnswerOptions() {
		Map<Integer, List<String>> optionsByQuestionId = new HashMap<>();
		String sql = "SELECT answers, questionid FROM answers ORDER BY questionid, id";
		try (Statement statement = getConnection().createStatement(); ResultSet rows = statement.executeQuery(sql)) {
			while (rows.next()) {
				int questionId = rows.getInt("questionid");
				boolean questionIdWasNull = rows.wasNull();
				String answerOption = rows.getString("answers");
				if (questionIdWasNull || questionId <= 0 || answerOption == null || answerOption.isBlank()) {
					throw new QuestionRepositoryException();
				}
				optionsByQuestionId.computeIfAbsent(questionId, ignored -> new ArrayList<>()).add(answerOption);
			}
		} catch (SQLException e) {
			throw new QuestionRepositoryException(e);
		}
		return optionsByQuestionId;
	}

	static Question mapQuestionRecord(Integer questionId, String text, int legacyDisplayType, String correctAnswer,
			List<String> answerOptions) {
		try {
			if (questionId == null || questionId <= 0 || text == null || text.isBlank() || correctAnswer == null
					|| correctAnswer.isBlank() || answerOptions == null
					|| answerOptions.stream().anyMatch(option -> option == null || option.isBlank())) {
				throw new IllegalArgumentException("Incomplete quiz question");
			}

			Question.DisplayType displayType = Question.DisplayType.fromLegacyValue(legacyDisplayType);
			boolean optionsAreComplete = switch (displayType) {
			case FOUR_BUTTONS -> answerOptions.size() >= 4;
			case DROP_DOWN -> !answerOptions.isEmpty();
			case TRUE_FALSE -> true;
			case FILL_IN_THE_BLANK -> answerOptions.size() >= 2;
			};
			if (!optionsAreComplete) {
				throw new IllegalArgumentException("Incomplete quiz answer options");
			}

			return new Question(questionId, text, displayType, correctAnswer, answerOptions);
		} catch (RuntimeException e) {
			if (e instanceof QuestionRepositoryException repositoryException) {
				throw repositoryException;
			}
			throw new QuestionRepositoryException(e);
		}
	}

	@Override
	public void close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			throw new QuestionRepositoryException(e);
		} finally {
			if (dbserver != null) {
				dbserver.stop();
			}
		}
	}

	public static void main(String[] args) {
		try (quizDAO dao = new quizDAO()) {
			dao.getConnection();
		}
	}
}
