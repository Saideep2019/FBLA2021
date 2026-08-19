package codingandProgramming.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.h2.tools.RunScript;

/**
 * Creates a new H2 database from the verified, versioned migration resources.
 */
final class DatabaseInitializer {

	private static final List<String> MIGRATIONS = List.of(
			"/db/migration/V1__quiz_schema.sql",
			"/db/migration/V2__quiz_content.sql");
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
			if (Files.isRegularFile(databaseFile)) {
				return;
			}
			if (Files.exists(databaseFile)) {
				throw new QuestionRepositoryException();
			}

			try {
				Path parent = databaseFile.getParent();
				if (parent != null) {
					Files.createDirectories(parent);
				}
				try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "")) {
					for (String migration : MIGRATIONS) {
						runMigration(connection, migration);
					}
				}
			} catch (IOException | SQLException | RuntimeException e) {
				if (e instanceof QuestionRepositoryException repositoryException) {
					throw repositoryException;
				}
				throw new QuestionRepositoryException(e);
			}
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
}
