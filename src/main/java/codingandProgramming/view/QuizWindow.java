package codingandProgramming.view;

/**
 * Lifecycle operations needed by the application coordinator.
 */
interface QuizWindow extends QuizView {

	void setSubmitHandler(Runnable submitHandler);

	void setCloseHandler(Runnable closeHandler);

	void showWindow();

	void disposeWindow();
}
