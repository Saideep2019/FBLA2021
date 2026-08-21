package codingandProgramming.view;

import java.awt.CardLayout;
import java.util.EnumMap;
import java.util.Map;

import codingandProgramming.model.Question;

/**
 * Switches between question presentations and owns their transient input state.
 */
final class QuestionCardPanel extends QuestionPanel {

	private final CardLayout cardLayout;
	private final Map<Question.DisplayType, QuestionPanel> panels = new EnumMap<>(Question.DisplayType.class);
	private Question.DisplayType activeType;

	QuestionCardPanel() {
		this(new CardLayout());
	}

	private QuestionCardPanel(CardLayout layout) {
		super(layout);
		cardLayout = layout;
		addPanel(Question.DisplayType.FOUR_BUTTONS, new FourButtonQuestionPanel());
		addPanel(Question.DisplayType.DROP_DOWN, new DropDownQuestionPanel());
		addPanel(Question.DisplayType.TRUE_FALSE, new TrueFalseQuestionPanel());
		addPanel(Question.DisplayType.FILL_IN_THE_BLANK, new FillInBlankQuestionPanel());
	}

	private void addPanel(Question.DisplayType type, QuestionPanel panel) {
		panels.put(type, panel);
		add(panel, type.name());
	}

	@Override
	void displayQuestion(Question question) {
		clearAnswer();
		activeType = question.getDisplayType();
		panels.get(activeType).displayQuestion(question);
		cardLayout.show(this, activeType.name());
		revalidate();
		repaint();
	}

	@Override
	String getSelectedAnswer() {
		return activeType == null ? "" : panels.get(activeType).getSelectedAnswer();
	}

	@Override
	void clearAnswer() {
		panels.values().forEach(QuestionPanel::clearAnswer);
	}

	Question.DisplayType getActiveType() {
		return activeType;
	}

	QuestionPanel getPanel(Question.DisplayType type) {
		return panels.get(type);
	}
}
