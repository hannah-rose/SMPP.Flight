package smpp.sensors.interfaces;

public interface RestListener {

	/**
	 * called when there is a change in the state of the sensor values, indicating
	 * a change in the rest status.
	 * @param isRestingNow true if the user is now resting.
	 */
	public void onRestChange(boolean isRestingNow);
}
