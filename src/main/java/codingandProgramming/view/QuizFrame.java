package codingandProgramming.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import codingandProgramming.model.Question;
import codingandProgramming.model.QuizReport;

/**
 * Creates and displays the main Swing quiz window.
 */
public final class QuizFrame extends JFrame implements QuizWindow {

	private final JLabel questionLabel = new JLabel(" ", SwingConstants.CENTER);
	private final JLabel scoreLabel = new JLabel("Score: 0");
	private final QuestionCardPanel questionCards = new QuestionCardPanel();
	private final JButton submitButton = new JButton("Submit Answer");
	private Runnable submitHandler = () -> {
	};
	private Runnable closeHandler = () -> {
	};

	QuizFrame(String studentName) {
		SwingThreading.requireEventDispatchThread();
		setTitle(studentName == null || studentName.isBlank() ? "Quiz App" : "Quiz App - " + studentName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(620, 360));
		buildContent();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				closeHandler.run();
			}
		});
	}

	private void buildContent() {
		JPanel content = new JPanel(new BorderLayout(16, 16));
		content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

		questionLabel.setFont(questionLabel.getFont().deriveFont(Font.BOLD, 16f));
		content.add(questionLabel, BorderLayout.NORTH);
		content.add(questionCards, BorderLayout.CENTER);

		JPanel footer = new JPanel(new BorderLayout(12, 0));
		footer.add(scoreLabel, BorderLayout.WEST);
		footer.add(submitButton, BorderLayout.EAST);
		content.add(footer, BorderLayout.SOUTH);
		setContentPane(content);
		getRootPane().setDefaultButton(submitButton);
		submitButton.addActionListener(event -> submitHandler.run());
	}

	@Override
	public void setSubmitHandler(Runnable submitHandler) {
		SwingThreading.requireEventDispatchThread();
		this.submitHandler = Objects.requireNonNull(submitHandler, "submitHandler");
	}

	@Override
	public void setCloseHandler(Runnable closeHandler) {
		SwingThreading.requireEventDispatchThread();
		this.closeHandler = Objects.requireNonNull(closeHandler, "closeHandler");
	}

	@Override
	public void showQuestion(Question question) {
		SwingThreading.requireEventDispatchThread();
		questionLabel.setText(question.getText());
		questionCards.displayQuestion(question);
	}

	@Override
	public String getSelectedAnswer() {
		SwingThreading.requireEventDispatchThread();
		return questionCards.getSelectedAnswer();
	}

	@Override
	public void clearAnswer() {
		SwingThreading.requireEventDispatchThread();
		questionCards.clearAnswer();
	}

	@Override
	public void showValidationError(String message) {
		SwingThreading.requireEventDispatchThread();
		JOptionPane.showMessageDialog(this, message, "Answer required", JOptionPane.WARNING_MESSAGE);
	}

	@Override
	public void showAnswerFeedback(boolean correct) {
		SwingThreading.requireEventDispatchThread();
		JOptionPane.showMessageDialog(this, correct ? "Right answer !" : "Wrong answer !");
	}

	@Override
	public void updateScore(int score) {
		SwingThreading.requireEventDispatchThread();
		scoreLabel.setText("Score: " + score);
	}

	@Override
	public void setSubmissionEnabled(boolean enabled) {
		SwingThreading.requireEventDispatchThread();
		submitButton.setEnabled(enabled);
	}

	@Override
	public void showReport(QuizReport report, String studentName) {
		SwingThreading.requireEventDispatchThread();
		JOptionPane.showMessageDialog(this,
				"Congratulations!, you have completed the quiz, press ok to view report");
		new QuizReportFrame(report, studentName).showWindow();
	}

	@Override
	public void showWindow() {
		SwingThreading.requireEventDispatchThread();
		pack();
		setSize(Math.max(680, getWidth()), Math.max(400, getHeight()));
		setLocationRelativeTo(null);
		setVisible(true);
	}

	@Override
	public void disposeWindow() {
		SwingThreading.requireEventDispatchThread();
		dispose();
	}

	QuestionCardPanel getQuestionCards() {
		return questionCards;
	}

	JButton getSubmitButton() {
		return submitButton;
	}
}
