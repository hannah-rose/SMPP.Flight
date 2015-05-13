package smpp.sensors.interfaces;

import smpp.sensors.TaskCompletionTracker;

public interface TaskCompletionListener {

	/**
	 * called when the task is completed, according criteria of the particular
	 * {@link TaskCompletionTracker}
	 */
	public void onTaskCompleted();
}
