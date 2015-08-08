package smpp.game.control;

import java.util.List;

import org.ric.smpp.domain.core.TrackingFrame;
import org.ric.smpp.domain.core.TrackingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smpp.game.Utils;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * class will be a {@link Control} which is responsible for making the spatial
 * it is attached to move as determined by the given motion data.
 * 
 * @author Jack
 * 
 */
public class MotionPlaybackController extends AbstractControl implements Control {
	private static final Logger LOG = LoggerFactory.getLogger(MotionPlaybackController.class);
	protected List<TrackingFrame> motion;
	protected long sTime = System.currentTimeMillis();
	protected int index = 0;
	protected String object;
	private final float scaling = 1f;
	//protected float[] distances;

	private Quaternion offset = new Quaternion().fromAngleAxis(3 * FastMath.PI / 2, Vector3f.UNIT_Y);

	/**
	 * a list of {@Link TrackingFrame}s containing the motion data which
	 * the spatial to which this control is attached.
	 * 
	 * @param motionData
	 * @throws Exception
	 */
	public MotionPlaybackController(String trackedObject, List<TrackingFrame> motionData)
			throws Exception {
		LOG.debug(MotionPlaybackController.class.getName()
				+ " constructed with motionData of length: " + motionData.size()
				+ " trackingObject: " + trackedObject);
		object = trackedObject;
		motion = motionData;
		if (motion.size() > 0) {
//			TrackingFrame d = motion.get(0);
//			distances = calculateDistances(motion, object);
		} else
			throw new Exception("motion data had length 0");
	}

	/**
	 * calculates cumulative distance traveled for each index, and returns that
	 * as an array
	 * 
	 * @param motion2
	 * @param object2
	 * @return
	 */
	private float[] calculateDistances(List<TrackingFrame> motion2, String object2) {
		float[] dists = new float[motion2.size()];
		Vector3f prev = Utils.pFromFloat(motion2.get(0).getTrackingObjects().get(object2)
				.getObjectPoint());
		dists[0] = 0;
		for (int i = 1; i < motion2.size(); i++) {
			TrackingFrame f = motion2.get(i);
			dists[i] = dists[i - 1]
					+ prev.distance(Utils.pFromFloat(f.getTrackingObjects().get(object2).getObjectPoint()));
		}
		return dists;
	}

	/**
	 * will set the given long as the 'start' time of the motion. That is, the
	 * difference between the current time and the given @param startTime will
	 * be considered the amount of time 'into' the motion that has elapsed. e.g.
	 * setting it to the currentTimeMillis() will cause the motion to start
	 * replaying from the beginning.
	 * 
	 * @param startTime
	 */
	public void setStart(long startTime) {
		this.sTime = startTime;
		index = 0;
	}

	@Override
	protected void controlUpdate(float tpf) {
		// nothing doing if motion is empty..
		if (motion.size() == 0) {
			this.setEnabled(false);
			LOG.warn(" motion had size 0. Nothing to playback...");
			return;
		}
		// could determine the time elapsed from tfp instead of currentTime()
		// call?
		index = currentTrackingFrameIndex(motion, sTime, index);
		if (index == motion.size()) {
			// we're out of motion to playback
			this.setEnabled(false);
			return;
		}
		// TODO: interpolation?
		// for now simply use the next soonest previous frame
		TrackingFrame frame = motion.get(index);
		TrackingObject to = frame.getTrackingObjects().get(object);
		if (to == null) {
			LOG.error("TrackingFrame did not contain TrackingObject with name: " + object);
			return;
		}
		float[] pos = to.getObjectPoint();
		if (pos == null || pos.length != 3) {
			LOG.error("TrackingObject " + object
					+ " did not contain Point data or contained oddly sized Point data");
			return;
		}
		float[] q = to.getObjectQuaternion();
		Vector3f newPos = new Vector3f(pos[0] / scaling, pos[1] / scaling, pos[2] / scaling);
		Quaternion quat = Utils.qFromFloat(q);
		quat = (quat == null) ? new Quaternion() : quat;
		LOG.debug("new MotionPlayback position: " + newPos);
		spatial.setLocalTranslation(newPos);
		spatial.setLocalRotation(quat.mult(offset));
	}

	@Override
	protected void controlRender(RenderManager arg0, ViewPort arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * returns the trackingFrame for the offset.
	 * 
	 * @param motion
	 * @param sTime
	 *            start time. Compared with currentTime produces time offset
	 *            into motion
	 * @param i current index. Just pass in 0 if not saved from last time.
	 * @return
	 */
	protected static int currentTrackingFrameIndex(List<TrackingFrame> motion, long sTime, int i) {
		if (i >= motion.size())
			return motion.size() - 1;
		long motionSTime = motion.get(0).getTimeStamp();
		long t = System.currentTimeMillis();
		long offset = t - sTime;
		LOG.debug("current offset: " + offset + " next motion offset: "
				+ (motion.get(i).getTimeStamp() - motionSTime));
		while (i < motion.size() && motion.get(i).getTimeStamp() - motionSTime < offset) {
			i++;
		}
		return i;
	}
}
