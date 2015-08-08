package smpp.game.control;

import org.ric.smpp.domain.common.eventnotifications.Observable;
import org.ric.smpp.domain.common.eventnotifications.Observer;
import org.ric.smpp.domain.core.TrackingFrame;
import org.ric.smpp.domain.core.TrackingObject;
import org.ric.smpp.domain.core.interfaces.IFrameData;
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

public class UserMotionControl extends AbstractControl implements Control, Observer<IFrameData> {

	private static final Logger LOG = LoggerFactory.getLogger(UserMotionControl.class);
	private String key;
	private Vector3f newPos = new Vector3f();
	private Quaternion quat = new Quaternion();
	private float multiplier = 1;
	/**
	 * model axis and threegear sensor axis are not aligned. transform data by
	 * offset to align axes so that plane is controlled nicely by hand
	 */
	private Quaternion offset = new Quaternion().fromAngleAxis(3 * FastMath.PI / 2, new Vector3f(0,
			1, 0));

	public UserMotionControl(String TrackedObject) {
		key = TrackedObject;
	}

	public UserMotionControl(String trackedObject, float translationMult) {
		this(trackedObject);
		this.multiplier = translationMult;
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void controlUpdate(float tpf) {
		if (newPos != null) {
			spatial.setLocalTranslation(newPos);
			spatial.setLocalRotation(quat);
		}
	}

	@Override
	public void update(Observable<IFrameData> object, IFrameData data) {
		if (data instanceof TrackingFrame) {
			TrackingFrame d = (TrackingFrame) data;
			TrackingObject to = d.getTrackingObjects().get(key);
			if (to == null) {
				LOG.warn("trackingFrame did not contain desired trackedObject: " + key + " frame: "
						+ d);
				return;
			}
			float[] p = to.getObjectPoint();
			float[] q = to.getObjectQuaternion();
			if (q != null) {
				quat = Utils.qFromFloat(q);
				quat = quat.mult(offset);
			}
			if (p != null)
				newPos = new Vector3f(p[0] * multiplier, p[1] * multiplier, p[2] * multiplier);
			// align plane model and data axes

			LOG.trace(UserMotionControl.class.getName() + " update: new TrackingFrame: " + d
					+ "new position: " + newPos + " new rotation: " + quat);
		}

	}

}
