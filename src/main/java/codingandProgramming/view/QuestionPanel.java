package codingandProgramming.view;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import codingandProgramming.model.Question;

/**
 * Common contract for one question presentation type.
 */
abstract class QuestionPanel extends JPanel {

	QuestionPanel(LayoutManager layout) {
		super(layout);
		SwingThreading.requireEventDispatchThread();
	}

	abstract void displayQuestion(Question question);

	abstract String getSelectedAnswer();

	abstract void clearAnswer();
}
