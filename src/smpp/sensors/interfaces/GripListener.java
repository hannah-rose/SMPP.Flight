package smpp.sensors.interfaces;

public interface GripListener {

	/**
	 * called when the grip state of the sensor changes. 
	 * @param isGrippingNow true if the status changed from not-gripping to gripping. false otherwise.
	 */
	public void onGripChange(boolean isGrippingNow);
}
