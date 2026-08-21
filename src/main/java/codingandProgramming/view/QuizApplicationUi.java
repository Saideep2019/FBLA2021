package codingandProgramming.view;

/**
 * Creates top-level Swing UI only after startup reaches the appropriate state.
 */
interface QuizApplicationUi {

	void showLoading();

	void hideLoading();

	String requestStudentName();

	QuizWindow createQuizWindow(String studentName);

	void showQuizWindow(QuizWindow window);

	void showUnavailable();
}
