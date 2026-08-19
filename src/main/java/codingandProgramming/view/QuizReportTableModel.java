package codingandProgramming.view;

import javax.swing.table.AbstractTableModel;

import codingandProgramming.model.QuizReport;
import codingandProgramming.model.QuizResult;

/**
 * Places quiz report values under their matching JTable columns.
 */
final class QuizReportTableModel extends AbstractTableModel {

	private static final String[] COLUMN_NAMES = {
			"Question", "Right Answer", "Selected Answer", "Is Answer Correct"
	};
	private static final int REPORT_ROW_COUNT = 50;
	private static final int SUMMARY_GAP = 3;

	private final Object[][] rows = new Object[REPORT_ROW_COUNT][COLUMN_NAMES.length];

	QuizReportTableModel(QuizReport report, String studentName) {
		int rowIndex = 0;
		for (QuizResult result : report.getRows()) {
			rows[rowIndex][0] = result.getQuestionText();
			rows[rowIndex][1] = result.getCorrectAnswer();
			rows[rowIndex][2] = result.getSelectedAnswer();
			rows[rowIndex][3] = result.isCorrect();
			rowIndex++;
		}

		rowIndex += SUMMARY_GAP;
		rows[rowIndex++][0] = "Student: " + studentName;
		rows[rowIndex++][0] = "   Questions attempted: " + report.getQuestionsAttempted();
		rows[rowIndex++][0] = "   Answered correctly: " + report.getCorrectAnswerCount();
		rows[rowIndex][0] = "   Percentage correct: " + report.getPercentageCorrect() + "%";
	}

	@Override
	public int getRowCount() {
		return rows.length;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return rows[rowIndex][columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
}
