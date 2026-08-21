package codingandProgramming.view;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import codingandProgramming.model.Question;

/**
 * Displays the True and False radio-button choices.
 */
final class TrueFalseQuestionPanel extends QuestionPanel {

	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final JRadioButton trueButton = new JRadioButton("True");
	private final JRadioButton falseButton = new JRadioButton("False");

	TrueFalseQuestionPanel() {
		super(new FlowLayout(FlowLayout.CENTER, 40, 20));
		trueButton.setActionCommand("true");
		falseButton.setActionCommand("false");
		buttonGroup.add(trueButton);
		buttonGroup.add(falseButton);
		add(trueButton);
		add(falseButton);
	}

	@Override
	void displayQuestion(Question question) {
		clearAnswer();
	}

	@Override
	String getSelectedAnswer() {
		return buttonGroup.getSelection() == null ? "" : buttonGroup.getSelection().getActionCommand();
	}

	@Override
	void clearAnswer() {
		buttonGroup.clearSelection();
	}

	JRadioButton getTrueButton() {
		return trueButton;
	}

	JRadioButton getFalseButton() {
		return falseButton;
	}
}
