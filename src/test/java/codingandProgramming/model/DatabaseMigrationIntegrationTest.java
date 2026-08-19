package codingandProgramming.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseMigrationIntegrationTest {

	private static final String QUESTIONS_CHECKSUM =
			"216B45179E5AD80DFB6A3EF95DBA84C4D2B755A3E0E9BB2859976F18B167170B";
	private static final String ANSWERS_CHECKSUM =
			"02A648A1B6F840A73FC08CC8B0BBDAAB6A314CC1783E1873A483DE23B01664CB";

	@Test
	void versionedContentMigrationUsesTheVerifiedLegacyInsertStatements() throws Exception {
		String verifiedExport = Files.readString(Path.of("database", "quiz-content.sql"), StandardCharsets.UTF_8);
		String migration = migrationResource("/db/migration/V2__quiz_content.sql");

		assertEquals(insertStatements(verifiedExport), insertStatements(migration));
	}

	@Test
	void freshDatabaseHasTheVerifiedH2Schema(@TempDir Path tempDirectory) throws Exception {
		Path databasePath = isolatedDatabasePath(tempDirectory, "schema");
		initialize(databasePath);

		try (Connection connection = connect(databasePath)) {
			assertEquals(List.of("ANSWERS", "QUESTIONS"), values(connection,
					"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
							+ "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME"));
			assertEquals(expectedColumns(), actualColumns(connection));
		}
	}

	@Test
	void freshDatabaseContentMatchesAllVerifiedStageOneFacts(@TempDir Path tempDirectory) throws Exception {
		Path databasePath = isolatedDatabasePath(tempDirectory, "content");
		initialize(databasePath);

		try (Connection connection = connect(databasePath)) {
			assertEquals(50, singleInt(connection, "SELECT COUNT(*) FROM QUESTIONS"));
			assertEquals(126, singleInt(connection, "SELECT COUNT(*) FROM ANSWERS"));
			assertEquals(42, singleInt(connection, "SELECT COUNT(DISTINCT QUESTIONID) FROM ANSWERS"));
			assertEquals(0, singleInt(connection, "SELECT COUNT(*) FROM ANSWERS A LEFT JOIN QUESTIONS Q "
					+ "ON A.QUESTIONID = Q.QUESTIONID WHERE Q.QUESTIONID IS NULL"));
			assertEquals(1, singleInt(connection, "SELECT MIN(QUESTIONID) FROM QUESTIONS"));
			assertEquals(50, singleInt(connection, "SELECT MAX(QUESTIONID) FROM QUESTIONS"));
			assertEquals(50, singleInt(connection, "SELECT COUNT(DISTINCT QUESTIONID) FROM QUESTIONS"));
			assertEquals(expectedQuestionIds(), integers(connection,
					"SELECT QUESTIONID FROM QUESTIONS ORDER BY QUESTIONID"));
			assertEquals(Map.of(1, 15, 2, 10, 3, 16, 4, 9), displayTypeCounts(connection));

			assertEquals("The currency of Poland is the Polish złoty", singleString(connection,
					"SELECT QUESTION FROM QUESTIONS WHERE QUESTIONID = 39"));
			assertEquals(List.of("Atlantic", "Pacific", "Indian", "Arctic"), answerOptions(connection, 15));
			assertEquals(List.of("An", "measures intelligence"), answerOptions(connection, 5));
			assertEquals(List.of("Do not use low-fat milk", "it will the taste (affect vs effect)"),
					answerOptions(connection, 11));
			assertEquals(List.of("The", "seperates both hemispheres"), answerOptions(connection, 28));

			assertEquals(QUESTIONS_CHECKSUM, checksum(connection,
					"SELECT QUESTIONID, QUESTION, DISPLAYTYPE, ANSWER FROM QUESTIONS ORDER BY QUESTIONID", 4));
			assertEquals(ANSWERS_CHECKSUM, checksum(connection,
					"SELECT QUESTIONID, ID, ANSWERS FROM ANSWERS ORDER BY QUESTIONID, ID, ANSWERS", 3));
		}
	}

	@Test
	void secondInitializationDoesNotDuplicateSchemaOrContent(@TempDir Path tempDirectory) throws Exception {
		Path databasePath = isolatedDatabasePath(tempDirectory, "repeat");
		initialize(databasePath);
		initialize(databasePath);

		try (Connection connection = connect(databasePath)) {
			assertEquals(50, singleInt(connection, "SELECT COUNT(*) FROM QUESTIONS"));
			assertEquals(126, singleInt(connection, "SELECT COUNT(*) FROM ANSWERS"));
		}
	}

	@Test
	void protectedLegacyDatabasePathsAreRejectedBeforeH2CanOpenThem() {
		try (quizDAO rootLegacyDatabase = new quizDAO(Path.of("quizdb"));
				quizDAO backupLegacyDatabase = new quizDAO(Path.of("database", "backup", "quizdb-original"))) {
			assertThrows(QuestionRepositoryException.class, rootLegacyDatabase::findAllQuestions);
			assertThrows(QuestionRepositoryException.class, backupLegacyDatabase::findAllQuestions);
		}
	}

	@Test
	void daoLoadsAllFiftyQuestionsFromTheMigratedDatabase(@TempDir Path tempDirectory) {
		Path databasePath = isolatedDatabasePath(tempDirectory, "dao");

		try (quizDAO dao = new quizDAO(databasePath)) {
			List<Question> questions = dao.findAllQuestions();

			assertEquals(50, questions.size());
			assertEquals(expectedQuestionIds(), questions.stream().map(Question::getId).toList());
		}
	}

	@Test
	void fiveQuestionQuizCanBeCompletedFromTheMigratedDatabase(@TempDir Path tempDirectory) {
		Path databasePath = isolatedDatabasePath(tempDirectory, "session");

		try (quizDAO dao = new quizDAO(databasePath)) {
			QuizSession session = new QuizSession(dao, new Random(2021L));
			while (!session.isComplete()) {
				Question question = session.getCurrentQuestion();
				session.submitAnswer(question.getCorrectAnswer());
			}

			assertEquals(QuizSession.SESSION_LENGTH, session.getScore());
			assertEquals(QuizSession.SESSION_LENGTH, session.getReport().getRows().size());
		}
	}

	private void initialize(Path databasePath) {
		try (quizDAO dao = new quizDAO(databasePath)) {
			assertEquals(50, dao.findAllQuestions().size());
		}
	}

	private Path isolatedDatabasePath(Path tempDirectory, String name) {
		Path databasePath = tempDirectory.resolve(name).toAbsolutePath().normalize();
		Path repository = Path.of("").toAbsolutePath().normalize();
		assertFalse(databasePath.startsWith(repository), "Test databases must stay outside the repository");
		return databasePath;
	}

	private Connection connect(Path databasePath) throws SQLException {
		return DriverManager.getConnection(databaseUrl(databasePath), "sa", "");
	}

	private String databaseUrl(Path databasePath) {
		return "jdbc:h2:file:" + databasePath.toString().replace('\\', '/');
	}

	private Map<String, ColumnDefinition> expectedColumns() {
		Map<String, ColumnDefinition> columns = new LinkedHashMap<>();
		columns.put("ANSWERS.ANSWERS", new ColumnDefinition("CHARACTER VARYING", "NO", 255L));
		columns.put("ANSWERS.QUESTIONID", new ColumnDefinition("INTEGER", "YES", null));
		columns.put("ANSWERS.ID", new ColumnDefinition("INTEGER", "YES", null));
		columns.put("QUESTIONS.QUESTION", new ColumnDefinition("CHARACTER VARYING", "NO", 255L));
		columns.put("QUESTIONS.QUESTIONID", new ColumnDefinition("INTEGER", "YES", null));
		columns.put("QUESTIONS.DISPLAYTYPE", new ColumnDefinition("INTEGER", "YES", null));
		columns.put("QUESTIONS.ANSWER", new ColumnDefinition("CHARACTER VARYING", "NO", 255L));
		return columns;
	}

	private Map<String, ColumnDefinition> actualColumns(Connection connection) throws SQLException {
		Map<String, ColumnDefinition> columns = new LinkedHashMap<>();
		String sql = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE, CHARACTER_MAXIMUM_LENGTH "
				+ "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC' "
				+ "ORDER BY TABLE_NAME, ORDINAL_POSITION";
		try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
			while (rows.next()) {
				long maximumLength = rows.getLong("CHARACTER_MAXIMUM_LENGTH");
				Long nullableMaximumLength = rows.wasNull() ? null : maximumLength;
				columns.put(rows.getString("TABLE_NAME") + "." + rows.getString("COLUMN_NAME"),
						new ColumnDefinition(rows.getString("DATA_TYPE"), rows.getString("IS_NULLABLE"),
								nullableMaximumLength));
			}
		}
		return columns;
	}

	private Map<Integer, Integer> displayTypeCounts(Connection connection) throws SQLException {
		Map<Integer, Integer> counts = new LinkedHashMap<>();
		try (Statement statement = connection.createStatement();
				ResultSet rows = statement.executeQuery(
						"SELECT DISPLAYTYPE, COUNT(*) AS ROW_COUNT FROM QUESTIONS GROUP BY DISPLAYTYPE ORDER BY DISPLAYTYPE")) {
			while (rows.next()) {
				counts.put(rows.getInt("DISPLAYTYPE"), rows.getInt("ROW_COUNT"));
			}
		}
		return counts;
	}

	private List<String> answerOptions(Connection connection, int questionId) throws SQLException {
		return values(connection,
				"SELECT ANSWERS FROM ANSWERS WHERE QUESTIONID = " + questionId + " ORDER BY ID, ANSWERS");
	}

	private List<Integer> expectedQuestionIds() {
		List<Integer> ids = new ArrayList<>();
		for (int id = 1; id <= 50; id++) {
			ids.add(id);
		}
		return ids;
	}

	private int singleInt(Connection connection, String sql) throws SQLException {
		try (Statement statement = connection.createStatement(); ResultSet row = statement.executeQuery(sql)) {
			row.next();
			return row.getInt(1);
		}
	}

	private String singleString(Connection connection, String sql) throws SQLException {
		try (Statement statement = connection.createStatement(); ResultSet row = statement.executeQuery(sql)) {
			row.next();
			return row.getString(1);
		}
	}

	private List<Integer> integers(Connection connection, String sql) throws SQLException {
		List<Integer> values = new ArrayList<>();
		try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
			while (rows.next()) {
				values.add(rows.getInt(1));
			}
		}
		return values;
	}

	private List<String> values(Connection connection, String sql) throws SQLException {
		List<String> values = new ArrayList<>();
		try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
			while (rows.next()) {
				values.add(rows.getString(1));
			}
		}
		return values;
	}

	private String checksum(Connection connection, String sql, int columnCount) throws Exception {
		StringBuilder canonicalContent = new StringBuilder();
		try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
			boolean firstRow = true;
			while (rows.next()) {
				if (!firstRow) {
					canonicalContent.append('\n');
				}
				firstRow = false;
				for (int column = 1; column <= columnCount; column++) {
					if (column > 1) {
						canonicalContent.append('|');
					}
					String value = rows.getString(column);
					canonicalContent.append(value.length()).append(':').append(value);
				}
			}
		}
		byte[] digest = MessageDigest.getInstance("SHA-256")
				.digest(canonicalContent.toString().getBytes(StandardCharsets.UTF_8));
		return HexFormat.of().withUpperCase().formatHex(digest);
	}

	private String migrationResource(String resourceName) throws IOException {
		try (InputStream input = DatabaseMigrationIntegrationTest.class.getResourceAsStream(resourceName)) {
			if (input == null) {
				throw new IOException("Missing test resource: " + resourceName);
			}
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private List<String> insertStatements(String sql) {
		return sql.lines().filter(line -> line.startsWith("INSERT INTO PUBLIC.")).toList();
	}

	private record ColumnDefinition(String type, String nullable, Long maximumLength) {
	}
}
