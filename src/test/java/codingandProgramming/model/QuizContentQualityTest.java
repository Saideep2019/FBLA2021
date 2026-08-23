package codingandProgramming.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuizContentQualityTest {

	@TempDir
	Path tempDirectory;

	private Path databasePath;

	@BeforeEach
	void createCorrectedDatabase() {
		databasePath = tempDirectory.resolve("quality").toAbsolutePath().normalize();
		Path repository = Path.of("").toAbsolutePath().normalize();
		assertFalse(databasePath.startsWith(repository), "Test databases must stay outside the repository");
		try (quizDAO dao = new quizDAO(databasePath)) {
			assertEquals(50, dao.findAllQuestions().size());
		}
	}

	@Test
	void questionIdentifiersAndRequiredFieldsAreComplete() throws Exception {
		try (Connection connection = connect()) {
			assertEquals(50, singleInt(connection, "SELECT COUNT(*) FROM QUESTIONS"));
			assertEquals(50, singleInt(connection, "SELECT COUNT(DISTINCT QUESTIONID) FROM QUESTIONS"));
			assertEquals(expectedQuestionIds(), integers(connection,
					"SELECT QUESTIONID FROM QUESTIONS ORDER BY QUESTIONID"));
			assertEquals(0, singleInt(connection,
					"SELECT COUNT(*) FROM QUESTIONS WHERE TRIM(QUESTION) = '' OR TRIM(ANSWER) = '' "
							+ "OR QUESTIONID IS NULL OR DISPLAYTYPE IS NULL"));
		}
	}

	@Test
	void displayTypesAndOptionCountsMatchTheirUiContracts() throws Exception {
		try (Connection connection = connect()) {
			assertEquals(0, singleInt(connection,
					"SELECT COUNT(*) FROM QUESTIONS WHERE DISPLAYTYPE NOT IN (1, 2, 3, 4)"));
			assertEquals(0, singleInt(connection, incorrectOptionCountSql(1, "<> 4")));
			assertEquals(0, singleInt(connection, incorrectOptionCountSql(2, "< 4")));
			assertEquals(0, singleInt(connection, incorrectOptionCountSql(3, "<> 2")));
			assertEquals(0, singleInt(connection, incorrectOptionCountSql(4, "<> 2")));
		}
	}

	@Test
	void choicesAreNonblankUniqueAndContainApplicableCorrectAnswersOnce() throws Exception {
		try (Connection connection = connect()) {
			assertEquals(0, singleInt(connection,
					"SELECT COUNT(*) FROM ANSWERS WHERE ANSWERS IS NULL OR TRIM(ANSWERS) = ''"));
			assertEquals(0, singleInt(connection,
					"SELECT COUNT(*) FROM (SELECT QUESTIONID, LOWER(TRIM(ANSWERS)), COUNT(*) AS C "
							+ "FROM ANSWERS GROUP BY QUESTIONID, LOWER(TRIM(ANSWERS)) HAVING COUNT(*) > 1)"));
			assertEquals(List.of(), integers(connection,
					"SELECT QUESTIONID FROM QUESTIONS Q WHERE Q.DISPLAYTYPE IN (1, 2, 3) AND "
							+ "(SELECT COUNT(*) FROM ANSWERS A WHERE A.QUESTIONID = Q.QUESTIONID "
							+ "AND LOWER(TRIM(A.ANSWERS)) = LOWER(TRIM(Q.ANSWER))) <> 1 ORDER BY QUESTIONID"));
		}
	}

	@Test
	void fillInTheBlankFragmentsRemainOrderedAndUsable() {
		try (quizDAO dao = new quizDAO(databasePath)) {
			for (Question question : dao.findAllQuestions()) {
				if (question.getDisplayType() == Question.DisplayType.FILL_IN_THE_BLANK) {
					assertEquals(2, question.getAnswerOptions().size(), "Question " + question.getId());
					assertFalse(question.getFirstBlankFragment().isBlank(), "Question " + question.getId());
					assertFalse(question.getSecondBlankFragment().isBlank(), "Question " + question.getId());
					assertTrue(question.getReportQuestion().contains(" ____ "), "Question " + question.getId());
				}
			}
		}
	}

	@Test
	void answerIdentifiersArePositiveUniqueStableAndNotOrphaned() throws Exception {
		try (Connection connection = connect()) {
			assertEquals(150, singleInt(connection, "SELECT COUNT(*) FROM ANSWERS"));
			assertEquals(150, singleInt(connection, "SELECT COUNT(DISTINCT ID) FROM ANSWERS"));
			assertEquals(0, singleInt(connection, "SELECT COUNT(*) FROM ANSWERS WHERE ID IS NULL OR ID <= 0"));
			assertEquals(0, singleInt(connection,
					"SELECT COUNT(*) FROM ANSWERS A LEFT JOIN QUESTIONS Q ON A.QUESTIONID = Q.QUESTIONID "
							+ "WHERE Q.QUESTIONID IS NULL"));
			assertEquals(0, singleInt(connection,
					"SELECT COUNT(*) FROM (SELECT QUESTIONID, ID, "
							+ "ROW_NUMBER() OVER (PARTITION BY QUESTIONID ORDER BY ID) AS POSITION FROM ANSWERS) "
							+ "WHERE ID <> QUESTIONID * 10 + POSITION"));
		}
	}

	@Test
	void requiredQuestionTwoAndThirtyOneCorrectionsAreExact() {
		try (quizDAO dao = new quizDAO(databasePath)) {
			List<Question> questions = dao.findAllQuestions();
			Question question2 = questionWithId(questions, 2);
			assertEquals("As of 2021, which country was the world's most populous?", question2.getText());
			assertEquals("China", question2.getCorrectAnswer());
			assertEquals(4, normalized(question2.getAnswerOptions()).stream().distinct().count());

			Question question31 = questionWithId(questions, 31);
			assertEquals("In which city is Harvard Yard located?", question31.getText());
			assertEquals("Cambridge", question31.getCorrectAnswer());
			assertEquals(List.of("Boston", "Cambridge", "New Haven", "New York City"),
					question31.getAnswerOptions());
		}
	}

	private String incorrectOptionCountSql(int displayType, String comparison) {
		return "SELECT COUNT(*) FROM QUESTIONS Q WHERE DISPLAYTYPE = " + displayType
				+ " AND (SELECT COUNT(DISTINCT LOWER(TRIM(A.ANSWERS))) FROM ANSWERS A "
				+ "WHERE A.QUESTIONID = Q.QUESTIONID) " + comparison;
	}

	private Connection connect() throws SQLException {
		String url = "jdbc:h2:file:" + databasePath.toString().replace('\\', '/');
		return DriverManager.getConnection(url, "sa", "");
	}

	private int singleInt(Connection connection, String sql) throws SQLException {
		try (Statement statement = connection.createStatement(); ResultSet row = statement.executeQuery(sql)) {
			row.next();
			return row.getInt(1);
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

	private List<String> normalized(List<String> values) {
		return values.stream().map(value -> value.trim().toLowerCase(Locale.ROOT)).toList();
	}
}
