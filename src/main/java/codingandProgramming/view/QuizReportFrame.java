package codingandProgramming.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import codingandProgramming.model.QuizReport;

/**
 * Displays the final quiz report without changing its data or column meanings.
 */
final class QuizReportFrame extends JFrame {

	QuizReportFrame(QuizReport report, String studentName) {
		SwingThreading.requireEventDispatchThread();
		setTitle(studentName == null || studentName.isBlank() ? "Quiz App Report" : "Quiz App - " + studentName);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JTable reportTable = new JTable(new QuizReportTableModel(report, studentName));
		reportTable.setShowGrid(false);
		reportTable.getColumnModel().getColumn(0).setPreferredWidth(500);
		reportTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		reportTable.getColumnModel().getColumn(2).setPreferredWidth(200);
		reportTable.getColumnModel().getColumn(3).setPreferredWidth(200);
		add(new JScrollPane(reportTable), BorderLayout.CENTER);
		setPreferredSize(new Dimension(850, 480));
	}

	void showWindow() {
		SwingThreading.requireEventDispatchThread();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
