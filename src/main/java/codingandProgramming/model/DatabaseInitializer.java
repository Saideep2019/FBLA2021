package codingandProgramming.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.h2.tools.RunScript;

/**
 * Creates a new H2 database from the verified, versioned migration resources.
 */
final class DatabaseInitializer {

	private static final String LEGACY_QUESTIONS_CHECKSUM =
			"216B45179E5AD80DFB6A3EF95DBA84C4D2B755A3E0E9BB2859976F18B167170B";
	private static final String LEGACY_ANSWERS_CHECKSUM =
			"02A648A1B6F840A73FC08CC8B0BBDAAB6A314CC1783E1873A483DE23B01664CB";
	private static final List<Migration> MIGRATIONS = List.of(
			new Migration("1", "Create quiz schema", "/db/migration/V1__quiz_schema.sql"),
			new Migration("2", "Import verified legacy quiz content", "/db/migration/V2__quiz_content.sql"),
			new Migration("3", "Correct and validate quiz content", "/db/migration/V3__quiz_content_corrections.sql"));
	private static final Object INITIALIZATION_LOCK = new Object();
	private static final Path LEGACY_DATABASE_FILE = Path.of("quizdb.mv.db").toAbsolutePath().normalize();
	private static final Path LEGACY_BACKUP_FILE = Path.of("database", "backup", "quizdb-original.mv.db")
			.toAbsolutePath().normalize();

	private final Path databaseFile;
	private final String databaseUrl;

	DatabaseInitializer(Path databasePath, String databaseUrl) {
		this.databaseFile = Path.of(databasePath.toString() + ".mv.db").toAbsolutePath().normalize();
		this.databaseUrl = databaseUrl;
	}

	void initialize() {
		synchronized (INITIALIZATION_LOCK) {
			protectLegacyDatabases();
			boolean databaseAlreadyExists = Files.isRegularFile(databaseFile);
			if (Files.exists(databaseFile) && !databaseAlreadyExists) {
				throw new QuestionRepositoryException();
			}

			try {
				Path parent = databaseFile.getParent();
				if (parent != null) {
					Files.createDirectories(parent);
				}
				try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "")) {
					prepareMigrationHistory(connection, databaseAlreadyExists);
					applyPendingMigrations(connection);
				}
			} catch (IOException | SQLException | RuntimeException e) {
				if (e instanceof QuestionRepositoryException repositoryException) {
					throw repositoryException;
				}
				throw new QuestionRepositoryException(e);
			}
		}
	}

	private void prepareMigrationHistory(Connection connection, boolean databaseAlreadyExists) throws SQLException {
		if (migrationHistoryExists(connection)) {
			return;
		}
		if (databaseAlreadyExists) {
			verifyLegacyV2Database(connection);
		}
		createMigrationHistory(connection);
		if (databaseAlreadyExists) {
			recordBaseline(connection);
		}
	}

	private boolean migrationHistoryExists(Connection connection) throws SQLException {
		try (ResultSet tables = connection.getMetaData().getTables(null, "PUBLIC", "SCHEMA_MIGRATIONS",
				new String[] { "TABLE" })) {
			return tables.next();
		}
	}

	private void createMigrationHistory(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE PUBLIC.SCHEMA_MIGRATIONS ("
					+ "VERSION VARCHAR(20) PRIMARY KEY, "
					+ "DESCRIPTION VARCHAR(255) NOT NULL, "
					+ "APPLIED_AT TIMESTAMP WITH TIME ZONE NOT NULL)");
		}
	}

	private void recordBaseline(Connection connection) throws SQLException {
		boolean originalAutoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		try {
			recordMigration(connection, MIGRATIONS.get(0));
			recordMigration(connection, MIGRATIONS.get(1));
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(originalAutoCommit);
		}
	}

	private void applyPendingMigrations(Connection connection) throws IOException, SQLException {
		List<String> appliedVersions = appliedVersions(connection);
		for (Migration migration : MIGRATIONS) {
			if (!appliedVersions.contains(migration.version())) {
				applyMigration(connection, migration);
			}
		}
	}

	private List<String> appliedVersions(Connection connection) throws SQLException {
		List<String> versions = new ArrayList<>();
		try (Statement statement = connection.createStatement();
				ResultSet rows = statement.executeQuery("SELECT VERSION FROM PUBLIC.SCHEMA_MIGRATIONS")) {
			while (rows.next()) {
				versions.add(rows.getString(1));
			}
		}
		return versions;
	}

	private void applyMigration(Connection connection, Migration migration) throws IOException, SQLException {
		boolean originalAutoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		try {
			runMigration(connection, migration.resource());
			recordMigration(connection, migration);
			connection.commit();
		} catch (IOException | SQLException | RuntimeException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(originalAutoCommit);
		}
	}

	private void recordMigration(Connection connection, Migration migration) throws SQLException {
		try (var statement = connection.prepareStatement(
				"INSERT INTO PUBLIC.SCHEMA_MIGRATIONS (VERSION, DESCRIPTION, APPLIED_AT) VALUES (?, ?, ?)")) {
			statement.setString(1, migration.version());
			statement.setString(2, migration.description());
			statement.setTimestamp(3, Timestamp.from(Instant.now()));
			statement.executeUpdate();
		}
	}

	private void verifyLegacyV2Database(Connection connection) throws SQLException {
		if (!legacySchemaMatches(connection)
				|| !LEGACY_QUESTIONS_CHECKSUM.equals(checksum(connection,
						"SELECT QUESTIONID, QUESTION, DISPLAYTYPE, ANSWER FROM QUESTIONS ORDER BY QUESTIONID", 4))
				|| !LEGACY_ANSWERS_CHECKSUM.equals(checksum(connection,
						"SELECT QUESTIONID, ID, ANSWERS FROM ANSWERS ORDER BY QUESTIONID, ID, ANSWERS", 3))) {
			throw new QuestionRepositoryException();
		}
	}

	private boolean legacySchemaMatches(Connection connection) throws SQLException {
		Map<String, ColumnDefinition> expected = new LinkedHashMap<>();
		expected.put("ANSWERS.ANSWERS", new ColumnDefinition("CHARACTER VARYING", "NO", 255L));
		expected.put("ANSWERS.QUESTIONID", new ColumnDefinition("INTEGER", "YES", null));
		expected.put("ANSWERS.ID", new ColumnDefinition("INTEGER", "YES", null));
		expected.put("QUESTIONS.QUESTION", new ColumnDefinition("CHARACTER VARYING", "NO", 255L));
		expected.put("QUESTIONS.QUESTIONID", new ColumnDefinition("INTEGER", "YES", null));
		expected.put("QUESTIONS.DISPLAYTYPE", new ColumnDefinition("INTEGER", "YES", null));
		expected.put("QUESTIONS.ANSWER", new ColumnDefinition("CHARACTER VARYING", "NO", 255L));

		Map<String, ColumnDefinition> actual = new LinkedHashMap<>();
		String sql = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE, CHARACTER_MAXIMUM_LENGTH "
				+ "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC' "
				+ "AND TABLE_NAME IN ('ANSWERS', 'QUESTIONS') ORDER BY TABLE_NAME, ORDINAL_POSITION";
		try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
			while (rows.next()) {
				long maximumLength = rows.getLong("CHARACTER_MAXIMUM_LENGTH");
				Long nullableMaximumLength = rows.wasNull() ? null : maximumLength;
				actual.put(rows.getString("TABLE_NAME") + "." + rows.getString("COLUMN_NAME"),
						new ColumnDefinition(rows.getString("DATA_TYPE"), rows.getString("IS_NULLABLE"),
								nullableMaximumLength));
			}
		}
		return expected.equals(actual);
	}

	private String checksum(Connection connection, String sql, int columnCount) throws SQLException {
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
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(canonicalContent.toString().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().withUpperCase().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private void protectLegacyDatabases() {
		if (databaseFile.equals(LEGACY_DATABASE_FILE) || databaseFile.equals(LEGACY_BACKUP_FILE)) {
			throw new QuestionRepositoryException();
		}
	}

	private void runMigration(Connection connection, String migration) throws IOException, SQLException {
		try (InputStream input = DatabaseInitializer.class.getResourceAsStream(migration)) {
			if (input == null) {
				throw new IOException("Missing database migration resource");
			}
			try (Reader script = new InputStreamReader(input, StandardCharsets.UTF_8)) {
				RunScript.execute(connection, script);
			}
		}
	}

	private record Migration(String version, String description, String resource) {
	}

	private record ColumnDefinition(String type, String nullable, Long maximumLength) {
	}
}
