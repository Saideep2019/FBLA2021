package codingandProgramming.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import codingandProgramming.model.Question;

class QuestionCardPanelTest {

	@Test
	void eachQuestionTypeActivatesOnlyItsIntendedPanelAndControls() throws Exception {
		onEdt(() -> {
			QuestionCardPanel cards = new QuestionCardPanel();
			FourButtonQuestionPanel buttons = (FourButtonQuestionPanel) cards
					.getPanel(Question.DisplayType.FOUR_BUTTONS);
			DropDownQuestionPanel dropDown = (DropDownQuestionPanel) cards
					.getPanel(Question.DisplayType.DROP_DOWN);
			TrueFalseQuestionPanel trueFalse = (TrueFalseQuestionPanel) cards
					.getPanel(Question.DisplayType.TRUE_FALSE);
			FillInBlankQuestionPanel fillBlank = (FillInBlankQuestionPanel) cards
					.getPanel(Question.DisplayType.FILL_IN_THE_BLANK);

			assertEquals(4, buttons.getAnswerButtons().size());
			assertEquals(0, dropDown.getAnswers().getItemCount());
			assertEquals("True", trueFalse.getTrueButton().getText());
			assertEquals("False", trueFalse.getFalseButton().getText());
			assertEquals(14, fillBlank.getAnswerField().getColumns());

			for (Question.DisplayType type : Question.DisplayType.values()) {
				cards.displayQuestion(questionFor(type));
				assertEquals(type, cards.getActiveType());
				for (Question.DisplayType candidate : Question.DisplayType.values()) {
					assertEquals(candidate == type, cards.getPanel(candidate).isVisible());
				}
			}
		});
	}

	@Test
	void switchingQuestionTypesClearsEveryStaleAnswer() throws Exception {
		onEdt(() -> {
			QuestionCardPanel cards = new QuestionCardPanel();
			FourButtonQuestionPanel buttons = (FourButtonQuestionPanel) cards
					.getPanel(Question.DisplayType.FOUR_BUTTONS);
			DropDownQuestionPanel dropDown = (DropDownQuestionPanel) cards
					.getPanel(Question.DisplayType.DROP_DOWN);
			TrueFalseQuestionPanel trueFalse = (TrueFalseQuestionPanel) cards
					.getPanel(Question.DisplayType.TRUE_FALSE);
			FillInBlankQuestionPanel fillBlank = (FillInBlankQuestionPanel) cards
					.getPanel(Question.DisplayType.FILL_IN_THE_BLANK);

			cards.displayQuestion(questionFor(Question.DisplayType.FOUR_BUTTONS));
			buttons.getAnswerButtons().get(0).doClick();
			assertEquals("Alpha", cards.getSelectedAnswer());

			cards.displayQuestion(questionFor(Question.DisplayType.DROP_DOWN));
			assertEquals("", cards.getSelectedAnswer());
			assertEquals("", buttons.getSelectedAnswer());
			dropDown.getAnswers().setSelectedIndex(1);
			assertEquals("Beta", cards.getSelectedAnswer());

			cards.displayQuestion(questionFor(Question.DisplayType.TRUE_FALSE));
			assertEquals(-1, dropDown.getAnswers().getSelectedIndex());
			trueFalse.getTrueButton().doClick();
			assertEquals("true", cards.getSelectedAnswer());

			cards.displayQuestion(questionFor(Question.DisplayType.FILL_IN_THE_BLANK));
			assertFalse(trueFalse.getTrueButton().isSelected());
			fillBlank.getAnswerField().setText("stale text");
			assertEquals("stale text", cards.getSelectedAnswer());

			cards.displayQuestion(questionFor(Question.DisplayType.FOUR_BUTTONS));
			assertEquals("", fillBlank.getAnswerField().getText());
			assertEquals("", cards.getSelectedAnswer());
			assertTrue(buttons.isVisible());
			assertFalse(fillBlank.isVisible());
		});
	}

	private Question questionFor(Question.DisplayType type) {
		List<String> options = type == Question.DisplayType.FILL_IN_THE_BLANK
				? List.of("Before", "after")
				: List.of("Alpha", "Beta", "Gamma", "Delta");
		return new Question(type.getLegacyValue(), "Question", type,
				type == Question.DisplayType.TRUE_FALSE ? "true" : "Alpha", options);
	}

	private void onEdt(Runnable action) throws Exception {
		SwingUtilities.invokeAndWait(action);
	}
}
