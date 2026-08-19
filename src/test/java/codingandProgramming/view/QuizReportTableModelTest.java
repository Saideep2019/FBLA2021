package codingandProgramming.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import codingandProgramming.model.Question;
import codingandProgramming.model.QuizReport;
import codingandProgramming.model.QuizSession;

class QuizReportTableModelTest {

	@Test
	void reportValuesAppearUnderTheCorrectColumnsAndSummaryRowsDoNotOverwriteEachOther() {
		QuizSession session = new QuizSession(
				() -> List.of(question(1), question(2), question(3), question(4), question(5)), new Random(44L));
		Question firstQuestion = session.getCurrentQuestion();
		String selectedAnswer = firstQuestion.getCorrectAnswer().toUpperCase();
		session.submitAnswer(selectedAnswer);
		for (int questionNumber = 1; questionNumber < QuizSession.SESSION_LENGTH; questionNumber++) {
			session.submitAnswer("wrong");
		}

		QuizReport report = session.getReport();
		QuizReportTableModel tableModel = new QuizReportTableModel(report, "Test Student");

		assertEquals("Question", tableModel.getColumnName(0));
		assertEquals("Right Answer", tableModel.getColumnName(1));
		assertEquals("Selected Answer", tableModel.getColumnName(2));
		assertEquals("Is Answer Correct", tableModel.getColumnName(3));
		assertEquals(firstQuestion.getText(), tableModel.getValueAt(0, 0));
		assertEquals(firstQuestion.getCorrectAnswer(), tableModel.getValueAt(0, 1));
		assertEquals(selectedAnswer, tableModel.getValueAt(0, 2));
		assertEquals(true, tableModel.getValueAt(0, 3));
		assertEquals(report.getRows().get(1).getQuestionText(), tableModel.getValueAt(1, 0));
		assertEquals(report.getRows().get(1).getCorrectAnswer(), tableModel.getValueAt(1, 1));
		assertEquals("wrong", tableModel.getValueAt(1, 2));
		assertEquals(false, tableModel.getValueAt(1, 3));
		assertEquals("Student: Test Student", tableModel.getValueAt(8, 0));
		assertEquals("   Questions attempted: 5", tableModel.getValueAt(9, 0));
		assertEquals("   Answered correctly: 1", tableModel.getValueAt(10, 0));
		assertEquals("   Percentage correct: 20%", tableModel.getValueAt(11, 0));
		assertFalse(tableModel.isCellEditable(0, 0));
	}

	private Question question(int id) {
		return new Question(id, "Question " + id, Question.DisplayType.DROP_DOWN, "answer " + id,
				List.of("answer " + id, "other"));
	}
}
