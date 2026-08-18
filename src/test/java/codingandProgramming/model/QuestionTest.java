package codingandProgramming.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class QuestionTest {

	@Test
	void fillInTheBlankQuestionRetainsBothTextFragments() {
		Question question = new Question(1, "Fill in the blanks", Question.DisplayType.FILL_IN_THE_BLANK,
				"middle", List.of("first fragment", "second fragment"));

		assertEquals("first fragment", question.getFirstBlankFragment());
		assertEquals("second fragment", question.getSecondBlankFragment());
		assertEquals("first fragment ____ second fragment", question.getReportQuestion());
	}

	@Test
	void allFourLegacyDisplayTypesCanBeRepresented() {
		assertEquals(Question.DisplayType.FOUR_BUTTONS, Question.DisplayType.fromLegacyValue(1));
		assertEquals(Question.DisplayType.DROP_DOWN, Question.DisplayType.fromLegacyValue(2));
		assertEquals(Question.DisplayType.TRUE_FALSE, Question.DisplayType.fromLegacyValue(3));
		assertEquals(Question.DisplayType.FILL_IN_THE_BLANK, Question.DisplayType.fromLegacyValue(4));
	}

	@Test
	void answerOptionsRetainTheirSuppliedOrdering() {
		List<String> suppliedOptions = List.of("fourth", "second", "first", "third");
		Question question = new Question(1, "Question", Question.DisplayType.FOUR_BUTTONS, "first",
				suppliedOptions);

		assertEquals(suppliedOptions, question.getAnswerOptions());
	}
}
