package codingandProgramming.view;

import javax.swing.SwingUtilities;

/**
 * Centralizes the Swing event-thread guard used by the view and controller.
 */
final class SwingThreading {

	private SwingThreading() {
	}

	static void requireEventDispatchThread() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Swing UI work must run on the Event Dispatch Thread");
		}
	}
}
