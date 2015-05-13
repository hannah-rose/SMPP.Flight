package smpp.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * class encapsulating configuration for a particular trial set.
 * 
 * @author Jack
 * 
 */
public class SetConfig {

	private float sensitivityAperture;
	private float sensitivityRotation;
	private float sensitivityTrajectory;
	private int numTrials;
	/**
	 * UUID for experiment whose data will be used as the control/healthy motion
	 * data for the set
	 */
	private String normId;
	/**
	 * UUID of the experiment to which data from this set will be saved
	 */
	private String expId;

	private Map<String, Boolean> feedback = new HashMap<String, Boolean>();

	public String getExpId() {
		return expId;
	}

	public void setExpId(String expId) {
		this.expId = expId;
	}

	public String getNormId() {
		return normId;
	}

	public void setNormId(String normId) {
		this.normId = normId;
	}

	public float getSensitivityAperture() {
		return sensitivityAperture;
	}

	public void setSensitivityAperture(float sensitivity_aperture) {
		this.sensitivityAperture = sensitivity_aperture;
	}

	public float getSensitivityRotation() {
		return sensitivityRotation;
	}

	public void setSensitivityRotation(float sensitivity_rotation) {
		this.sensitivityRotation = sensitivity_rotation;
	}

	public float getSensitivityTrajectory() {
		return sensitivityTrajectory;
	}

	public void setSensitivityTrajectory(float sensitivity_trajectory) {
		this.sensitivityTrajectory = sensitivity_trajectory;
	}

	public int getNumTrials() {
		return numTrials;
	}

	public void setNumTrials(int num_trials) {
		this.numTrials = num_trials;
	}

	public Map<String, Boolean> getFeedback() {
		return feedback;
	}

	public void setFeedback(Map<String, Boolean> feedback) {
		this.feedback = feedback;
	}
}
