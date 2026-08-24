package codingandProgramming.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

import org.h2.tools.RunScript;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseMigrationIntegrationTest {

	private static final String LEGACY_QUESTIONS_CHECKSUM =
			"216B45179E5AD80DFB6A3EF95DBA84C4D2B755A3E0E9BB2859976F18B167170B";
	private static final String LEGACY_ANSWERS_CHECKSUM =
			"02A648A1B6F840A73FC08CC8B0BBDAAB6A314CC1783E1873A483DE23B01664CB";
	private static final String CORRECTED_QUESTIONS_CHECKSUM =
			"69728FED47DFC100F805D9A48C0017514A85ADCB8DB0201F9B4EB7E08FB053D2";
	private static final String CORRECTED_ANSWERS_CHECKSUM =
			"0A9BE30CA16B40D53D533D8292F194CA36DC387B7777F9E5E3E8EB5DF6C94334";

	@Test
	void versionedContentMigrationUsesTheVerifiedLegacyInsertStatements() throws Exception {
		String verifiedExport = Files.readString(Path.of("database", "quiz-content.sql"), StandardCharsets.UTF_8);
		String migration = migrationResource("/db/migration/V2__quiz_content.sql");

		assertEquals(insertStatements(verifiedExport), insertStatements(migration));
	}

	@Test
	void preservedV1AndV2ContentRetainsTheStageOneHistoricalChecksums(@TempDir Path tempDirectory) throws Exception {
		Path databasePath = isolatedDatabasePath(tempDirectory, "legacy-checksums");
		try (Connection connection = connect(databasePath)) {
			runResource(connection, "/db/migration/V1__quiz_schema.sql");
			runResource(connection, "/db/migration/V2__quiz_content.sql");

			assertEquals(50, singleInt(connection, "SELECT COUNT(*) FROM QUESTIONS"));
			assertEquals(126, singleInt(connection, "SELECT COUNT(*) FROM ANSWERS"));
			assertEquals(LEGACY_QUESTIONS_CHECKSUM, checksum(connection,
					"SELECT QUESTIONID, QUESTION, DISPLAYTYPE, ANSWER FROM QUESTIONS ORDER BY QUESTIONID", 4));
			assertEquals(LEGACY_ANSWERS_CHECKSUM, checksum(connection,
					"SELECT QUESTIONID, ID, ANSWERS FROM ANSWERS ORDER BY QUESTIONID, ID, ANSWERS", 3));
		}
	}

	@Test
	void freshDatabaseHasQuizSchemaAndMigrationHistory(@TempDir Path tempDirectory) throws Exception {
		Path databasePath = isolatedDatabasePath(tempDirectory, "schema");
		initialize(databasePath);

		try (Connection connection = connect(databasePath)) {
			assertEquals(List.of("ANSWERS", "QUESTIONS", "SCHEMA_MIGRATIONS"), values(connection,
					"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
							+ "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME"));
			assertEquals(expectedColumns(), actualColumns(connection));
			assertEquals(List.of("1", "2", "3"), values(connection,
					"SELECT VERSION FROM SCHEMA_MIGRATIONS ORDER BY CAST(VERSION AS INTEGER)"));
			assertEquals(3, singleInt(connection,
					"SELECT COUNT(*) FROM SCHEMA_MIGRATIONS WHERE APPLIED_AT IS NOT NULL"));
		}
	}

	@Test
	void freshDatabaseContentMatchesTheCorrectedRuntimeChecksums(@TempDir Path tempDirectory) throws Exception {
		Path databasePath = isolatedDatabasePath(tempDirectory, "corrected-checksums");
		initialize(databasePath);

		try (Connection connection = connect(databasePath)) {
			assertEquals(CORRECTED_QUESTIONS_CHECKSUM, checksum(connection,
					"SELECT QUESTIONID, QUESTION, DISPLAYTYPE, ANSWER FROM QUESTIONS ORDER BY QUESTIONID", 4));
			assertEquals(CORRECTED_ANSWERS_CHECKSUM, checksum(connection,
					"SELECT QUESTIONID, ID, ANSWERS FROM ANSWERS ORDER BY QUESTIONID, ID, ANSWERS", 3));
		}
	}

	@Test
	void existingVerifiedV2DatabaseIsBaselinedAndReceivesV3Once(@TempDir Path tempDirectory) throws Exception {
		Path databasePath = isolatedDatabasePath(tempDirectory, "existing-v2");
		createV2Database(databasePath);

		new DatabaseInitializer(databasePath, databaseUrl(databasePath)).initialize();
		new DatabaseInitializer(databasePath, databaseUrl(databasePath)).initialize();

		try (Connection connection = connect(databasePath)) {
			assertEquals(List.of("1", "2", "3"), values(connection,
					"SELECT VERSION FROM SCHEMA_MIGRATIONS ORDER BY CAST(VERSION AS INTEGER)"));
			assertEquals(1, singleInt(connection,
					"SELECT COUNT(*) FROM SCHEMA_MIGRATIONS WHERE VERSION = '3'"));
			assertEquals("As of 2021, which country was the world's most populous?", singleString(connection,
					"SELECT QUESTION FROM QUESTIONS WHERE QUESTIONID = 2"));
			assertEquals(150, singleInt(connection, "SELECT COUNT(*) FROM ANSWERS"));
		}
	}

	@Test
	void unverifiedExistingDatabaseWithoutHistoryIsRejected(@TempDir Path tempDirectory) throws Exception {
		Path databasePath = isolatedDatabasePath(tempDirectory, "invalid-baseline");
		createV2Database(databasePath);
		try (Connection connection = connect(databasePath); Statement statement = connection.createStatement()) {
			statement.executeUpdate("UPDATE QUESTIONS SET ANSWER = 'India' WHERE QUESTIONID = 2");
		}

		assertThrows(QuestionRepositoryException.class,
				() -> new DatabaseInitializer(databasePath, databaseUrl(databasePath)).initialize());
	}

	@Test
	void failedV3RollsBackAndIsNotRecordedAsSuccessful(@TempDir Path tempDirectory) throws Exception {
		Path databasePath = isolatedDatabasePath(tempDirectory, "failed-v3");
		createV2Database(databasePath);
		try (Connection connection = connect(databasePath); Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE SCHEMA_MIGRATIONS (VERSION VARCHAR(20) PRIMARY KEY, "
					+ "DESCRIPTION VARCHAR(255) NOT NULL, APPLIED_AT TIMESTAMP WITH TIME ZONE NOT NULL)");
			statement.execute("INSERT INTO SCHEMA_MIGRATIONS VALUES "
					+ "('1', 'Create quiz schema', CURRENT_TIMESTAMP), "
					+ "('2', 'Import verified legacy quiz content', CURRENT_TIMESTAMP)");
			statement.execute("ALTER TABLE QUESTIONS ADD CONSTRAINT BLOCK_Q2_CORRECTION "
					+ "CHECK (QUESTIONID <> 2 OR QUESTION = 'What country has the largest population?')");
		}

		assertThrows(QuestionRepositoryException.class,
				() -> new DatabaseInitializer(databasePath, databaseUrl(databasePath)).initialize());

		try (Connection connection = connect(databasePath)) {
			assertEquals(List.of("1", "2"), values(connection,
					"SELECT VERSION FROM SCHEMA_MIGRATIONS ORDER BY CAST(VERSION AS INTEGER)"));
			assertEquals("Which is the largest state in USA?", singleString(connection,
					"SELECT QUESTION FROM QUESTIONS WHERE QUESTIONID = 1"));
			assertEquals("What country has the largest population?", singleString(connection,
					"SELECT QUESTION FROM QUESTIONS WHERE QUESTIONID = 2"));
		}
	}

	@Test
	void repeatedInitializationDoesNotDuplicateSchemaOrContent(@TempDir Path tempDirectory) throws Exception {
		Path databasePath = isolatedDatabasePath(tempDirectory, "repeat");
		initialize(databasePath);
		initialize(databasePath);

		try (Connection connection = connect(databasePath)) {
			assertEquals(50, singleInt(connection, "SELECT COUNT(*) FROM QUESTIONS"));
			assertEquals(150, singleInt(connection, "SELECT COUNT(*) FROM ANSWERS"));
			assertEquals(3, singleInt(connection, "SELECT COUNT(*) FROM SCHEMA_MIGRATIONS"));
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
	void daoLoadsAllCorrectedQuestions(@TempDir Path tempDirectory) {
		Path databasePath = isolatedDatabasePath(tempDirectory, "dao");

		try (quizDAO dao = new quizDAO(databasePath)) {
			List<Question> questions = dao.findAllQuestions();

			assertEquals(50, questions.size());
			assertEquals(expectedQuestionIds(), questions.stream().map(Question::getId).toList());
			Question question2 = questionWithId(questions, 2);
			assertEquals("As of 2021, which country was the world's most populous?", question2.getText());
			Question question31 = questionWithId(questions, 31);
			assertEquals("Cambridge", question31.getCorrectAnswer());
			assertEquals(List.of("Boston", "Cambridge", "New Haven", "New York City"),
					question31.getAnswerOptions());
		}
	}

	@Test
	void fiveQuestionQuizCanBeCompletedFromTheCorrectedDatabase(@TempDir Path tempDirectory) {
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

	private void createV2Database(Path databasePath) throws Exception {
		try (Connection connection = connect(databasePath)) {
			runResource(connection, "/db/migration/V1__quiz_schema.sql");
			runResource(connection, "/db/migration/V2__quiz_content.sql");
		}
	}

	private void runResource(Connection connection, String resourceName) throws Exception {
		try (InputStream input = DatabaseMigrationIntegrationTest.class.getResourceAsStream(resourceName)) {
			assertNotNull(input, "Missing test migration resource: " + resourceName);
			try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
				RunScript.execute(connection, reader);
			}
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
		columns.put("SCHEMA_MIGRATIONS.VERSION", new ColumnDefinition("CHARACTER VARYING", "NO", 20L));
		columns.put("SCHEMA_MIGRATIONS.DESCRIPTION", new ColumnDefinition("CHARACTER VARYING", "NO", 255L));
		columns.put("SCHEMA_MIGRATIONS.APPLIED_AT", new ColumnDefinition("TIMESTAMP WITH TIME ZONE", "NO", null));
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

	private List<Integer> expectedQuestionIds() {
		List<Integer> ids = new ArrayList<>();
		for (int id = 1; id <= 50; id++) {
			ids.add(id);
		}
		return ids;
	}

	private Question questionWithId(List<Question> questions, int id) {
		return questions.stream().filter(question -> question.getId() == id).findFirst().orElseThrow();
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
