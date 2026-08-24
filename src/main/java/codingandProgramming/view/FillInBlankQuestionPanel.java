package codingandProgramming.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JTextField;

import codingandProgramming.model.Question;

/**
 * Displays sentence fragments around a free-text answer field.
 */
final class FillInBlankQuestionPanel extends QuestionPanel {

	private final JLabel firstFragment = new JLabel();
	private final JTextField answer = new JTextField(14);
	private final JLabel secondFragment = new JLabel();

	FillInBlankQuestionPanel() {
		super(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(4, 4, 4, 4);
		constraints.gridx = 0;
		add(firstFragment, constraints);
		constraints.gridx = 1;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(answer, constraints);
		constraints.gridx = 2;
		constraints.weightx = 0;
		constraints.fill = GridBagConstraints.NONE;
		add(secondFragment, constraints);
	}

	@Override
	void displayQuestion(Question question) {
		firstFragment.setText(question.getFirstBlankFragment());
		secondFragment.setText(question.getSecondBlankFragment());
		clearAnswer();
	}

	@Override
	String getSelectedAnswer() {
		return answer.getText();
	}

	@Override
	void clearAnswer() {
		answer.setText("");
	}

	JTextField getAnswerField() {
		return answer;
	}
}
