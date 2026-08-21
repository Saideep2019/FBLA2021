package codingandProgramming.view;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;

import codingandProgramming.model.Question;

/**
 * Displays the legacy four-answer button presentation.
 */
final class FourButtonQuestionPanel extends QuestionPanel {

	private static final int ANSWER_COUNT = 4;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final List<JToggleButton> answerButtons = new ArrayList<>();

	FourButtonQuestionPanel() {
		super(new GridLayout(2, 2, 12, 12));
		for (int index = 0; index < ANSWER_COUNT; index++) {
			JToggleButton button = new JToggleButton();
			buttonGroup.add(button);
			answerButtons.add(button);
			add(button);
		}
	}

	@Override
	void displayQuestion(Question question) {
		clearAnswer();
		List<String> options = question.getAnswerOptions();
		for (int index = 0; index < answerButtons.size(); index++) {
			String option = options.get(index);
			JToggleButton button = answerButtons.get(index);
			button.setText(option);
			button.setActionCommand(option);
		}
	}

	@Override
	String getSelectedAnswer() {
		return buttonGroup.getSelection() == null ? "" : buttonGroup.getSelection().getActionCommand();
	}

	@Override
	void clearAnswer() {
		buttonGroup.clearSelection();
	}

	List<JToggleButton> getAnswerButtons() {
		return List.copyOf(answerButtons);
	}
}
