package codingandProgramming.view;

import java.awt.GridBagLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import codingandProgramming.model.Question;

/**
 * Displays a single drop-down answer control.
 */
final class DropDownQuestionPanel extends QuestionPanel {

	private final JComboBox<String> answers = new JComboBox<>();

	DropDownQuestionPanel() {
		super(new GridBagLayout());
		add(answers);
	}

	@Override
	void displayQuestion(Question question) {
		answers.setModel(new DefaultComboBoxModel<>(question.getAnswerOptions().toArray(String[]::new)));
		clearAnswer();
	}

	@Override
	String getSelectedAnswer() {
		Object selection = answers.getSelectedItem();
		return selection == null ? "" : selection.toString();
	}

	@Override
	void clearAnswer() {
		answers.setSelectedIndex(-1);
	}

	JComboBox<String> getAnswers() {
		return answers;
	}
}
