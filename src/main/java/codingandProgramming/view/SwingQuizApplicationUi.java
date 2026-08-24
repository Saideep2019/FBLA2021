package codingandProgramming.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import codingandProgramming.model.QuestionRepositoryException;

/**
 * Desktop Swing implementation of application-level prompts and windows.
 */
final class SwingQuizApplicationUi implements QuizApplicationUi {

	private JWindow loadingWindow;

	@Override
	public void showLoading() {
		SwingThreading.requireEventDispatchThread();
		loadingWindow = new JWindow();
		loadingWindow.getContentPane().setLayout(new BorderLayout());
		URL splashImage = SwingQuizApplicationUi.class.getResource("realquizbk.jfif");
		JLabel splashLabel = splashImage == null
				? new JLabel("Loading Quiz App…", SwingConstants.CENTER)
				: new JLabel(new ImageIcon(splashImage));
		splashLabel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
		loadingWindow.add(splashLabel, BorderLayout.CENTER);
		loadingWindow.pack();
		if (loadingWindow.getWidth() < 320 || loadingWindow.getHeight() < 180) {
			loadingWindow.setSize(new Dimension(Math.max(320, loadingWindow.getWidth()),
					Math.max(180, loadingWindow.getHeight())));
		}
		loadingWindow.setLocationRelativeTo(null);
		loadingWindow.setVisible(true);
	}

	@Override
	public void hideLoading() {
		SwingThreading.requireEventDispatchThread();
		if (loadingWindow != null) {
			loadingWindow.dispose();
			loadingWindow = null;
		}
	}

	@Override
	public String requestStudentName() {
		SwingThreading.requireEventDispatchThread();
		return JOptionPane.showInputDialog(null, "Please enter your name to begin quiz", "Quiz App",
				JOptionPane.QUESTION_MESSAGE);
	}

	@Override
	public QuizWindow createQuizWindow(String studentName) {
		SwingThreading.requireEventDispatchThread();
		return new QuizFrame(studentName);
	}

	@Override
	public void showQuizWindow(QuizWindow window) {
		SwingThreading.requireEventDispatchThread();
		window.showWindow();
	}

	@Override
	public void showUnavailable() {
		SwingThreading.requireEventDispatchThread();
		JOptionPane.showMessageDialog(null, QuestionRepositoryException.USER_MESSAGE, "Quiz unavailable",
				JOptionPane.ERROR_MESSAGE);
	}
}
