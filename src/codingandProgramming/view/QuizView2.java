package codingandProgramming.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import codingandProgramming.model.OutOfQuestionsException;
import codingandProgramming.model.QuestionAndOptionsModel;
import codingandProgramming.model.quizDAO;

/**
 * Main View class for Quiz App This class is responsible for the UI of the
 * program. Works with the DAO to retrieve data
 * 
 * @author Saideep Ambari
 *
 */
public class QuizView2 {
	/**
	 * Defining variables and fields for use later on in the class.
	 */
	private JFrame frmQuizApp;
	public static JLabel questionLabel = new JLabel("");
	public static JLabel scoreLabel = new JLabel("Score");
	public static JLabel realScoreLabel = new JLabel("");
	public static JButton btnOne = new JButton("New Button");
	public static JButton btnTwo = new JButton("New Button");
	public static JButton btnThree = new JButton("New Button");
	public static JButton btnFour = new JButton("New Button");
	public static JButton btnFive = new JButton("New Button");
	public static JRadioButton trueRadioButton = new JRadioButton("New radio button");
	public static JComboBox<String> comboBox = new JComboBox<String>();
	public static JRadioButton falseRadioButton = new JRadioButton("New radio button");
	public quizDAO dao = new quizDAO();
	public String one = "";
	public String two = "";
	public String three = "";
	public String four = "";
	public String selectedAnswer = "";
	public int score = 0;
	public QuestionAndOptionsModel model = null;
	public Map<Integer, String> providedAnswersMap = new HashMap<Integer, String>();
	public static boolean didtheycompletethequestion = false;
	public static JLabel fillIntheblankLabelOne = new JLabel("New label");
	public static JLabel fillintheblanklabelTwo = new JLabel("New label");
	public static JTextField fillIntheblankfield = new JTextField();;
	public String[][] reportArr = new String[50][5];
	public int currentQuestionIndex = 0;
	String tableColumns[] = { "Question", "Right Answer", "Selected Answer", "Is Answer Correct" };
	public double percentCorrect = score / 5;
	public boolean isCorrect;
	public boolean isAttempted;
	public static String nameOfStudent = "";

	public static void main(String[] args) throws SQLException {

		JFrame frame = QuizView2.displaySplashScreenInNewThread();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					frame.dispose();

					QuizView2 window = new QuizView2();
					nameOfStudent = JOptionPane.showInputDialog("Please enter your name to begin quiz");
					window.frmQuizApp.setTitle("Quiz App - " + nameOfStudent);
					window.model = window.dao.getRandomQuestionAndAnswers();
					window.displayProperWidgets(window.model);

					window.frmQuizApp.setVisible(true);
					window.initialize2dArray();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static JFrame displaySplashScreenInNewThread() {
		JFrame frame = new JFrame();

		URL url = null;
		try {
			url = Class.forName("codingandProgramming.view.QuizView2").getResource("realquizbk.jfif");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ImageIcon ic = new ImageIcon(url);
		JLabel splashLabel = new JLabel("");
		splashLabel.setIcon(ic);
		frame.getContentPane().add(splashLabel);
		frame.setTitle("Quiz App V1.1");
		frame.pack(); // automatically size the window to fit its components
		frame.setLocationRelativeTo(null); // center this window on the screen
		frame.setVisible(true);
		return frame;
	}

	private static JFrame displayAnotherSplashInNewThread() {
		JFrame frame = new JFrame();

		URL url = null;
		try {
			url = Class.forName("codingandProgramming.view.QuizView2").getResource("correctanswergif.gif");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ImageIcon ic = new ImageIcon(url);
		JLabel splashLabel = new JLabel("");
		splashLabel.setIcon(ic);
		frame.getContentPane().add(splashLabel);
		frame.setTitle("Quiz App V1.1");
		frame.pack(); // automatically size the window to fit its components
		frame.setLocationRelativeTo(null); // center this window on the screen
		frame.setVisible(true);
		return frame;
	}

	protected void initialize2dArray() {
		// ""

	}

	private final Action actionOne = new btnOneAction();
	private final Action actionTwo = new btnTwoAction();
	private final Action actionThree = new btnThreeAction();
	private final Action actionFour = new btnFourAction();
	private final Action nextQuestionAction = new nextQuestionAction();
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final Action trueButtonAction = new SwingAction();
	private final Action falseButtonAction = new SwingAction_1();
	private boolean fillInBlankFlag;

	public QuizView2() throws SQLException {

		initialize();

	}

	/**
	 * Initialize the contents of the frame. Defines things such as the frame,
	 * buttons, textfields, and other UI components.
	 */
	private void initialize() {

		frmQuizApp = new JFrame();
		// frmQuizApp.setLocationRelativeTo(null);

		frmQuizApp.getContentPane().setForeground(Color.ORANGE);
		// frmQuizApp.setTitle("Quiz App " );
		frmQuizApp.setBounds(100, 100, 450, 300);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// Calculates the position where the CenteredJFrame
		// should be paced on the screen.
		int x = (screenSize.width - frmQuizApp.getWidth()) / 2;
		int y = (screenSize.height - frmQuizApp.getHeight()) / 2;
		frmQuizApp.setLocation(x, y);

		frmQuizApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmQuizApp.getContentPane().setLayout(null);
		btnOne.setFont(new Font("Tahoma", Font.PLAIN, 9));
		btnOne.setAction(actionOne);

		btnOne.setBounds(10, 78, 187, 23);
		frmQuizApp.getContentPane().add(btnOne);
		btnThree.setFont(new Font("Tahoma", Font.PLAIN, 9));
		btnThree.setAction(actionThree);

		btnThree.setBounds(224, 82, 200, 23);
		frmQuizApp.getContentPane().add(btnThree);
		btnTwo.setFont(new Font("Tahoma", Font.PLAIN, 9));
		btnTwo.setAction(actionTwo);

		btnTwo.setBounds(10, 142, 187, 23);
		frmQuizApp.getContentPane().add(btnTwo);
		btnFour.setFont(new Font("Tahoma", Font.PLAIN, 9));
		btnFour.setAction(actionFour);

		btnFour.setBounds(224, 142, 200, 23);
		frmQuizApp.getContentPane().add(btnFour);
		questionLabel.setBounds(10, 12, 414, 23);
		frmQuizApp.getContentPane().add(questionLabel);
		scoreLabel.setBounds(10, 209, 84, 14);
		frmQuizApp.getContentPane().add(scoreLabel);
		frmQuizApp.getContentPane().add(realScoreLabel);
		realScoreLabel.setBounds(69, 209, 46, 14);
		// frame = displayAnotherSplashInNewThread
		frmQuizApp.getContentPane().add(realScoreLabel);

		JButton nextQuestionbtn = new JButton("New button");
		nextQuestionbtn.setAction(nextQuestionAction);
		nextQuestionbtn.setBounds(283, 205, 120, 23);
		frmQuizApp.getContentPane().add(nextQuestionbtn);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedAnswer = (String) comboBox.getSelectedItem();
			}
		});

		comboBox.setBounds(73, 82, 30, 22);
		frmQuizApp.getContentPane().add(comboBox);

		comboBox.setBounds(111, 116, 198, 22);
		frmQuizApp.getContentPane().add(comboBox);

		trueRadioButton.setAction(trueButtonAction);
		buttonGroup.add(trueRadioButton);
		trueRadioButton.setBounds(75, 78, 109, 23);
		trueRadioButton.setVisible(false);
		frmQuizApp.getContentPane().add(trueRadioButton);

		falseRadioButton.setAction(falseButtonAction);
		buttonGroup.add(falseRadioButton);
		falseRadioButton.setBounds(241, 82, 120, 23);
		falseRadioButton.setVisible(false);
		frmQuizApp.getContentPane().add(falseRadioButton);
		fillIntheblankLabelOne.setForeground(Color.BLUE);
		fillIntheblankLabelOne.setFont(new Font("Tahoma", Font.PLAIN, 10));

		fillIntheblankLabelOne.setBounds(21, 55, 163, 16);
		frmQuizApp.getContentPane().add(fillIntheblankLabelOne);

		fillIntheblankfield.setBounds(166, 51, 96, 20);
		frmQuizApp.getContentPane().add(fillIntheblankfield);
		fillIntheblankfield.setColumns(10);
		fillintheblanklabelTwo.setForeground(Color.BLUE);
		fillintheblanklabelTwo.setFont(new Font("Tahoma", Font.PLAIN, 10));

		fillintheblanklabelTwo.setBounds(274, 56, 173, 14);
		frmQuizApp.getContentPane().add(fillintheblanklabelTwo);
		comboBox.setVisible(false);

	}

	private class btnOneAction extends AbstractAction {
		public btnOneAction() {
			putValue(NAME, "Button 1");
			putValue(SHORT_DESCRIPTION, "");
		}

		public void actionPerformed(ActionEvent e) {
			selectedAnswer = btnOne.getText();

		}
	}

	private class btnTwoAction extends AbstractAction {
		public btnTwoAction() {
			putValue(NAME, "SwingAction_1");
			putValue(SHORT_DESCRIPTION, "");
		}

		public void actionPerformed(ActionEvent e) {
			selectedAnswer = btnTwo.getText();

		}
	}

	private class btnThreeAction extends AbstractAction {
		public btnThreeAction() {
			putValue(NAME, "SwingAction_2");
			putValue(SHORT_DESCRIPTION, "");
		}

		public void actionPerformed(ActionEvent e) {
			selectedAnswer = btnThree.getText();
		}
	}

	private class btnFourAction extends AbstractAction {
		public btnFourAction() {
			putValue(NAME, "SwingAction_3");
			putValue(SHORT_DESCRIPTION, "");
		}

		public void actionPerformed(ActionEvent e) {
			selectedAnswer = btnFour.getText();

		}
	}

	private class nextQuestionAction extends AbstractAction {
		public nextQuestionAction() {
			putValue(NAME, "Next Question");
			putValue(SHORT_DESCRIPTION, "");
		}

		private void buildDataForReport() {
			// before going to next question check if chosen answer is right
			providedAnswersMap.put(model.getQuestionId(), selectedAnswer);

			reportArr[currentQuestionIndex][0] = model.getQuestion();
			reportArr[currentQuestionIndex][1] = model.getRightAnswer();
			// System.out.println(selectedAnswer);
			reportArr[currentQuestionIndex][2] = selectedAnswer;
			reportArr[currentQuestionIndex][3] = isCorrect + "";
			reportArr[4][4] = percentCorrect + "";

			if (model.getDisplayType() == 4) {
				List<String> options = model.getOptions();
				reportArr[currentQuestionIndex][0] = options.get(0) + " ____ " + options.get(1);
			}

			currentQuestionIndex++;
		}

		public void actionPerformed(ActionEvent e) {

			if (fillInBlankFlag) {
				selectedAnswer = fillIntheblankfield.getText();
				fillInBlankFlag = false;
			}

			Boolean answer = false;
			try {
				answer = selectedAnswer.equalsIgnoreCase(model.getRightAnswer());
			} catch (Exception e1) {

			}

			if (answer) {
				URL url = null;
				isCorrect = true;
				JWindow splash = new JWindow();
				try {
					url = Class.forName("codingandProgramming.view.QuizView2").getResource("flower.gif");
				} catch (ClassNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				if (url != null) {

					ImageIcon icon = new ImageIcon(url);
					splash.getContentPane().add(new JLabel("", icon, SwingConstants.CENTER));// centering the splash
																								// screen
					splash.setBounds(550, 160, 400, 400);// setting the size and coordinates of the splash screen.
					splash.setVisible(true);
					try {
						Thread.sleep(2000);// duration of the splash screen
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					splash.setVisible(false);// setting the visibility of the splash screen to false, after 3000
												// milliseconds.

				}

				JOptionPane.showMessageDialog(null, "You got the question correct");
				score++;

				scoreLabel.setText("Score: " + score);

				selectedAnswer = "";

			}

			if (selectedAnswer == "") {
				selectedAnswer = "";

			}

			else {
				isCorrect = false;

				JOptionPane.showMessageDialog(null, "You got the question incorrect");

				JWindow splashOne = new JWindow();
				java.net.URL imgURL = JWindow.class.getResource("/wronganswergif.gif");

				if (imgURL != null) {
					ImageIcon icon = new ImageIcon(imgURL);
					splashOne.getContentPane().add(new JLabel("", icon, SwingConstants.CENTER));// centering the splash
																								// screen
					splashOne.setBounds(550, 160, 400, 400);// setting the size and coordinates of the splash screen.
					splashOne.setVisible(true);
					try {
						Thread.sleep(2000);// duration of the splash screen
					} catch (InterruptedException e2) {
						e2.printStackTrace();
					}
					splashOne.setVisible(false);// setting the visibility of the splash screen to false, after 3000
												// milliseconds.

				}

			}

			buildDataForReport();

			try {
				model = dao.getRandomQuestionAndAnswers();
			} catch (OutOfQuestionsException e1) {
				// System.out.println("Ran out of questions");
				JOptionPane.showMessageDialog(null,
						"Congratulations!, you have completed the quiz, press ok to view report");
				reportView();
			} // get model for next question

			displayProperWidgets(model);

		}

		private void reportView() {

			currentQuestionIndex = currentQuestionIndex + 3;
			reportArr[currentQuestionIndex++][0] = "Student: " + nameOfStudent;
			reportArr[currentQuestionIndex++][0] = "   Questions attempted: " + 5;
			reportArr[currentQuestionIndex++][0] = "   Answered correctly: " + score;

			reportArr[currentQuestionIndex++][0] = "   Percentage correct: " + (score * 100 / 5);

			JFrame reportFrame = new JFrame();
			JTable reportTable = new JTable(reportArr, tableColumns);
			reportFrame.setSize(600, 500);
			reportFrame.setTitle("Quiz App - " + nameOfStudent);
			// dgdfgdfg
			// tdgdfg
			// gfthgfhg
			reportFrame.getContentPane().add(new JScrollPane(reportTable));
			reportFrame.setVisible(true);
			reportTable.setVisible(true);
			reportTable.isCellEditable(5, 3);
			reportTable.setShowGrid(false);

			reportTable.getColumnModel().getColumn(0).setPreferredWidth(500);
			reportTable.getColumnModel().getColumn(1).setPreferredWidth(200);
			reportTable.getColumnModel().getColumn(2).setPreferredWidth(200);
			reportTable.getColumnModel().getColumn(3).setPreferredWidth(200);

			// JOptionPane.showMessageDialog(null, "Number of Questions Answered Correctly"
			// + " " + score);
			// e1.printStackTrace();
		}

	}

	/**
	 * The following methods are used to display the correct type of questions. For
	 * example the showfillintheblanks method makes all other types of questions
	 * invisible but makes the fill in the blanks question visible. The same concept
	 * is repeated for all other types of questions.
	 * 
	 * @param model
	 */
	public void displayProperWidgets(QuestionAndOptionsModel model) {

		fillIntheblankLabelOne.setVisible(false);
		fillintheblanklabelTwo.setVisible(false);
		fillIntheblankfield.setVisible(false);

		comboBox.setVisible(false);

		btnOne.setVisible(false);
		btnTwo.setVisible(false);
		btnThree.setVisible(false);
		btnFour.setVisible(false);

		if (model.getDisplayType() == 1)
			showButtons(model);

		if (model.getDisplayType() == 2)
			showDropDown(model);

		if (model.getDisplayType() == 3)
			showRadioButton(model);

		if (model.getDisplayType() == 4) {
			showFillInBlanks(model);
			fillInBlankFlag = true;
		}

	}

	private void showFillInBlanks(QuestionAndOptionsModel model) {

		btnOne.setVisible(false);
		btnTwo.setVisible(false);
		btnThree.setVisible(false);
		btnFour.setVisible(false);

		trueRadioButton.setVisible(false);
		falseRadioButton.setVisible(false);

		comboBox.setVisible(false);

		fillIntheblankLabelOne.setText(model.getOptions().get(0));
		fillintheblanklabelTwo.setText(model.getOptions().get(1));

		fillIntheblankLabelOne.setVisible(true);
		fillintheblanklabelTwo.setVisible(true);
		fillIntheblankfield.setVisible(true);
		questionLabel.setVisible(false);
		fillIntheblankfield.setText("");
	}

	public void showButtons(QuestionAndOptionsModel model) {

		trueRadioButton.setVisible(false);

		falseRadioButton.setVisible(false);
		fillIntheblankLabelOne.setVisible(false);
		fillintheblanklabelTwo.setVisible(false);
		fillIntheblankfield.setVisible(false);

		comboBox.setVisible(false);

		String one = "";
		String two = "";
		String three = "";
		String four = "";

		Iterator it = model.getOptions().iterator();

		while (it.hasNext()) {
			if (one == "") {
				one = (String) it.next();
				continue;
			}
			if (two == "") {
				two = (String) it.next();
				continue;
			}
			if (three == "") {
				three = (String) it.next();
				continue;
			}

			if (four == "") {
				four = (String) it.next();
				continue;
			}

		}

		btnOne.setText(one);
		btnTwo.setText(two);
		btnThree.setText(three);
		btnFour.setText(four);

		btnOne.setVisible(true);
		btnTwo.setVisible(true);
		btnThree.setVisible(true);
		btnFour.setVisible(true);

		questionLabel.setText(model.getQuestion());
		questionLabel.setVisible(true);

	}

	public void showDropDown(QuestionAndOptionsModel model) {
		btnOne.setVisible(false);
		btnTwo.setVisible(false);
		btnThree.setVisible(false);
		btnFour.setVisible(false);

		trueRadioButton.setVisible(false);

		falseRadioButton.setVisible(false);
		fillIntheblankLabelOne.setVisible(false);
		fillintheblanklabelTwo.setVisible(false);
		fillIntheblankfield.setVisible(false);

		questionLabel.setText(model.getQuestion());
		questionLabel.setVisible(true);

		comboBox.setModel(
				new DefaultComboBoxModel<String>(model.getOptions().toArray(new String[model.getOptions().size()])));
		comboBox.setVisible(true);

		comboBox.setSelectedItem(null);
	}

	/**
	 * This method is responsible for showing the radiobuttons. It makes all other
	 * types of questions invisible, and only shows the radiobuttons.
	 * 
	 * @param model
	 */
	public void showRadioButton(QuestionAndOptionsModel model) {

		btnOne.setVisible(false);
		btnTwo.setVisible(false);
		btnThree.setVisible(false);
		btnFour.setVisible(false);

		comboBox.setVisible(false);

		questionLabel.setText(model.getQuestion());
		questionLabel.setVisible(true);

		fillIntheblankLabelOne.setVisible(false);
		fillintheblanklabelTwo.setVisible(false);

		trueRadioButton.setVisible(true);
		falseRadioButton.setVisible(true);
		trueRadioButton.setText("True");
		falseRadioButton.setText("False");

		trueRadioButton.setSelected(false);
		falseRadioButton.setSelected(false);
		buttonGroup.setSelected(null, false);

	}

	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "True");
			putValue(SHORT_DESCRIPTION, "Press to choose true");
		}

		public void actionPerformed(ActionEvent e) {
			didtheycompletethequestion = true;// sets variable to true if they selected an answer

			if (trueRadioButton.isSelected()) {
				selectedAnswer = "true";
			}

		}
	}

	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
			putValue(NAME, "False");
			putValue(SHORT_DESCRIPTION, "Press to choose false");
		}

		public void actionPerformed(ActionEvent e) {
			didtheycompletethequestion = true;// sets variable to true if they selected an answer

			if (falseRadioButton.isSelected()) {

				selectedAnswer = "false";
			}

		}
	}
}
