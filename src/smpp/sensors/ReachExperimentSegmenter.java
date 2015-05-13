package smpp.sensors;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.apache.commons.lang.NullArgumentException;
import org.ric.smpp.analysis.EvenWindowSizeException;
import org.ric.smpp.analysis.Motion;
import org.ric.smpp.analysis.MovingAvgSmoothingStrategy;
import org.ric.smpp.core.NonPositiveTimeDeltaException;
import org.ric.smpp.domain.common.Common.TRACKING_DATA_TYPE;
import org.ric.smpp.domain.common.eventnotifications.Observable;
import org.ric.smpp.domain.common.eventnotifications.Observer;
import org.ric.smpp.domain.core.TrackingFrame;
import org.ric.smpp.domain.core.TrackingObject;
import org.ric.smpp.domain.core.interfaces.IFrameData;
import org.ric.smpp.domain.serialclient.SerialSensorFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smpp.sensors.interfaces.ISegmenter;

/**
 * Class which determines which 'phase' of the reach the subject is in by
 * observing {@link IFrameData} updates from various sensors. In this case,
 * SerialRestObject and either ThreeGear or OptiTrack motion data.
 * 
 * @author Jack
 * 
 */
public class ReachExperimentSegmenter implements ISegmenter<ReachExperimentSegmenter.Phase>,
		Observer<IFrameData> {

	public enum Phase {
		REST, REACH, REACHEND, RETURN, NONE
	}

	private static final Logger LOG = LoggerFactory.getLogger(ReachExperimentSegmenter.class);

	private float speedThreshold = 5;

	private Phase lastPhase = Phase.NONE;
	private Phase curPhase;
	// private boolean isResting = false;
	private Motion myMotion;
	private SerialSensorFrame lastRestPadFrame = new SerialSensorFrame(5,
			TRACKING_DATA_TYPE.SerialRestPad);
	private SerialSensorFrame lastObjectFrame = new SerialSensorFrame(16,
			TRACKING_DATA_TYPE.SerialObject);

	private final double reachLowerVelBound = 1;
	private final float targetRadius;
	private final Point3f targetPosition;
	private final Point3f restPosition;
	private final String toName;
	
	private LiftTaskCompletionTracker liftTaskCompletionTracker;

	public ReachExperimentSegmenter(String trackedObject, float targetRadius,
			Point3f targetPosition, Point3f restPosition) throws EvenWindowSizeException {
		this.myMotion = new Motion(new MovingAvgSmoothingStrategy(5));
		this.targetPosition = targetPosition;
		this.restPosition = restPosition;
		this.targetRadius = targetRadius;
		this.toName = trackedObject;
		for (int i = 0; i < lastRestPadFrame.getSensorValues().length; i++)
			lastRestPadFrame.getSensorValues()[i] = -1;
	}

	public Phase getCurrentPhase() {
		return curPhase;
	}

	private void setPhase(Phase newPhase) {
		lastPhase = curPhase;
		curPhase = newPhase;
	}

	@Override
	public void update(Observable<IFrameData> object, IFrameData data) {
		switch (data.getFrameSource()) {
		case SerialObject:
			break;
		case SerialRestPad:
			SerialSensorFrame ssf = (SerialSensorFrame) data;
			LOG.info("SerialRestPad. values[2]: " + ssf.getSensorValues()[2]);
			float[] values = ssf.getSensorValues();
			switch (ssf.getFrameSource()) {
			case SerialObject:
				lastObjectFrame = ssf;
				break;
			case SerialRestPad:
				lastRestPadFrame = ssf;
			}

			break;
		case ThreeGearData:
		case OptiTrackData:
			// update phase based on Optitrack or 3Gear data
			TrackingFrame tf = (TrackingFrame) data;
			TrackingObject to = tf.getTrackingObjects().get(toName);
			if (to != null) {
				Vector3f v = new Vector3f(to.getObjectPoint());
				Point3f p = new Point3f(to.getObjectPoint());
				LOG.info("TrackingFrame update. Point: " + p);

				try {
					myMotion.addData(v, tf.getTimeStamp());
				} catch (NullArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NonPositiveTimeDeltaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// if we were reaching, and we are now close enough to target,
				// and
				// velocity below threshold. end reach phase.
				if (withinRadius(targetRadius, targetPosition, p) && curPhase == Phase.REACH
						&& myMotion.speedBelowThreshold(speedThreshold)) {
					setPhase(Phase.REACHEND);
				} else if (!withinRadius(targetRadius, targetPosition, p)
						&& curPhase == Phase.REACHEND
						&& !myMotion.speedBelowThreshold(speedThreshold))
					setPhase(Phase.RETURN);
			} else
				LOG.warn("couldn't find TrackedObject named: " + toName
						+ " in TrackingFrame from source: " + tf.getFrameSource());
			break;
		default:
			LOG.warn("in default tracking_frame case");
		}

		// check new values against segment criteria
		if (isResting(lastRestPadFrame) && myMotion.speedBelowValue(0.025))
			setPhase(Phase.REST);
		else if (!isResting(lastRestPadFrame) && !myMotion.speedBelowValue(0.025)
		// TODO: should this be curPhase?
				&& lastPhase == Phase.REST)
			setPhase(Phase.REACH);
		LOG.info("leaving ReachExperiment update. phase is: " + curPhase + " currentSpeed: "
				+ myMotion.mostRecentSpeed());
	}

	private static boolean withinRadius(float radius, Point3f target, Point3f p) {
		return (p.distance(target) < radius);
	}

	/**
	 * give this the {@link SerialRestPad} sensor values, and returns true if
	 * the subject is resting, false otherwise.
	 * 
	 * @param values
	 * @return
	 */
	private static boolean isResting(SerialSensorFrame f) {
		if (f.getFrameSource() != TRACKING_DATA_TYPE.SerialRestPad) {
			LOG.info("isResting expects " + TRACKING_DATA_TYPE.SerialRestPad
					+ " data frames. returning false. given: " + f.getFrameSource());
			return false;
		}
		float v = f.getSensorValues()[2];
		return v == 1;
	}

}