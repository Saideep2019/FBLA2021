/**
 * Loads quiz content from the preserved H2 database for the Swing application.
 */
package codingandProgramming.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.h2.tools.Server;

public class quizDAO implements QuestionRepository {

	private static final String host = "jdbc:h2:file:./quizdb";
	public static final String driver = "org.h2.Driver";
	private static final int LEGACY_FIRST_QUESTION_ID = 1;
	private static final int LEGACY_QUESTION_ID_UPPER_BOUND = 49;

	String userid = "sa";
	String password = "";
	public static int studentidtochange;
	public static final int NUMBEROFQUESTIONS = QuizSession.SESSION_LENGTH;

	private Server dbserver;
	private Connection connection;

	public quizDAO() {
		try {
			dbserver = Server.createTcpServer().start();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection() {
		try {
			if (dbserver == null) {
				dbserver = Server.createTcpServer().start();
			}

			if (connection == null) {
				Class.forName(driver);
				connection = DriverManager.getConnection(host, userid, password);
			}
			return connection;
		} catch (ClassNotFoundException | SQLException e) {
			throw new IllegalStateException("Unable to connect to the quiz database", e);
		}
	}

	@Override
	public List<Question> findAllQuestions() {
		Map<Integer, List<String>> optionsByQuestionId = loadAnswerOptions();
		List<Question> questions = new ArrayList<>();
		String sql = "SELECT question, questionid, displaytype, answer FROM questions "
				+ "WHERE questionid >= " + LEGACY_FIRST_QUESTION_ID + " AND questionid < "
				+ LEGACY_QUESTION_ID_UPPER_BOUND + " ORDER BY questionid";

		try (Statement statement = getConnection().createStatement(); ResultSet rows = statement.executeQuery(sql)) {
			while (rows.next()) {
				int questionId = rows.getInt("questionid");
				questions.add(new Question(questionId, rows.getString("question"),
						Question.DisplayType.fromLegacyValue(rows.getInt("displaytype")), rows.getString("answer"),
						optionsByQuestionId.getOrDefault(questionId, List.of())));
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to load quiz questions", e);
		}
		return questions;
	}

	private Map<Integer, List<String>> loadAnswerOptions() {
		Map<Integer, List<String>> optionsByQuestionId = new HashMap<>();
		String sql = "SELECT answers, questionid FROM answers WHERE questionid >= " + LEGACY_FIRST_QUESTION_ID
				+ " AND questionid < " + LEGACY_QUESTION_ID_UPPER_BOUND + " ORDER BY questionid, id";
		try (Statement statement = getConnection().createStatement(); ResultSet rows = statement.executeQuery(sql)) {
			while (rows.next()) {
				optionsByQuestionId.computeIfAbsent(rows.getInt("questionid"), ignored -> new ArrayList<>())
						.add(rows.getString("answers"));
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to load quiz answer options", e);
		}
		return optionsByQuestionId;
	}

	static boolean isLegacyEligibleQuestionId(int questionId) {
		return questionId >= LEGACY_FIRST_QUESTION_ID && questionId < LEGACY_QUESTION_ID_UPPER_BOUND;
	}

	public static void main(String[] args) {
		new quizDAO().getConnection();
	}
}
