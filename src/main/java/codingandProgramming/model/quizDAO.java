/**
 * This class is responsible for establishing the connecting between JAVA and MYSQL.
 * THis is used to retrieve the information from the database, which will then be used to populate the fields and information in the GUI.
 * This follows the Single Responsibility Principle to prevent bugs in the code and keep things more organized.
 */
package codingandProgramming.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import org.h2.tools.Server;

public class quizDAO {

	private Server dbserver = null;

	// private static final String host =
	// "jdbc:h2:tcp://localhost/./quizdb;AUTO_SERVER=TRUE";// establishing the host
	// connection to the url

	private static final String host = "jdbc:h2:file:./quizdb";
	public static final String driver = "org.h2.Driver";
	String userid = "sa"; // "postgres";
	String password = ""; // "admin";
	public static int studentidtochange;
	public static int NUMBEROFQUESTIONS = 5;
	private Connection connection = null;
	private ArrayList<Integer> completedQuestionsList = new ArrayList<Integer>();
	private ArrayList<Integer> completedQuestionTypes = new ArrayList<Integer>();

	// Defining a list to store the information from the database.

	private Connection getConnection() {// Creating a method to establish the connection
		try {
			if (dbserver == null) // start h2 db if it is not already started
				dbserver = Server.createTcpServer().start();

			if (connection == null) {// if the connection is not already established, the connection will get
										// established with the following code.
				try {
					Class.forName(driver).newInstance();// Using the driver that is necessary to establish the
														// connection to the DERBY database
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {// catching any
																										// exceptions
																										// that could
																										// arise, from
																										// not
																										// establishing
																										// the
																										// connection.
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				connection = DriverManager.getConnection(host, userid, password);// establishing the connection to the
																					// host which is declared above.
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;

	}

	public quizDAO() {
		try {
			dbserver = Server.createTcpServer().start();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public QuestionAndOptionsModel getRandomQuestionAndAnswers() throws OutOfQuestionsException {

		int low = 1; // inclusive
		int high = 49; // exclusive

		if (completedQuestionsList.size() >= NUMBEROFQUESTIONS) {
			throw new OutOfQuestionsException();

		}

		java.sql.Statement statement = null;
		// defining statement, so the connection can be established
		ResultSet rs = null;
		// getting the information from the query and storing it in a resultset.
		QuestionAndOptionsModel model = new QuestionAndOptionsModel();
		int questionId = 0;
		Random r = new Random();
		int randomNumber = r.nextInt(high - low) + low;// Random number between 2 numbers.

		// Making sure that the randomNumber that is generated is unique. This is done
		// by checking if the randomNumber is in the list, and then adding that over and
		// over.

		while (completedQuestionsList.contains(randomNumber)) {
			// System.out.println("looping " + randomNumber);
			randomNumber = r.nextInt(high - low) + low;

		}

		String getQuestionSQL = "SELECT * FROM questions WHERE questionid = " + randomNumber + " ";
		try {
			statement = getConnection().createStatement();
			rs = statement.executeQuery(getQuestionSQL);
			while (rs.next()) {
				// looping through the resulset and storing important information

				model.setQuestion(rs.getString(1));
				questionId = rs.getInt(2);
				model.setDisplayType(rs.getInt(3));
				model.setRightAnswer(rs.getString(4));
				// Storing all the useful information from the database such as the question,
				// questionID, displayType, and right answer into the model object.

			}

			completedQuestionsList.add(randomNumber);
			completedQuestionTypes.add(model.getDisplayType());

			// Getting the corresponding answer that matches with the specific questionID
			// that was retrieved in the previous query
			String getOptionsql = "SELECT  answers FROM answers WHERE questionid = " + questionId + "" + " ORDER BY ID";
			statement = getConnection().createStatement();
			rs = statement.executeQuery(getOptionsql);
			while (rs.next()) {
				// getting the food from the database, and looping through it and adding it to
				// the list.
				model.addOption(rs.getString(1));
				// adding the new object back to the list

			}

		}

		catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(model.getQuestion() + "---" + model.getQuestionId());
		return model;

	}

	public static void main(String args[]) {

		quizDAO dao = new quizDAO();
		dao.getConnection();

		// dao.insertId();

	}

}
