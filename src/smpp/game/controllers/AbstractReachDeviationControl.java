package smpp.game.controllers;

import java.util.ArrayList;
import java.util.List;

import org.ric.smpp.domain.core.TrackingFrame;
import org.ric.smpp.domain.core.TrackingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smpp.configuration.Configuration;
import smpp.game.Utils;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;
import smpp.game.controllers.MotionPlaybackController;

/*
 * TODO: should probably do some kind of function fit to the control data and
 * use that. Seems bad trying to do this directly with
 * straight TrackingFrames.
 */
/**
 * abstract class
 * implementing functions for a control which moves a spatial along a path, and
 * deviated by the amount that the user's motion deviates from a healthy path.
 * 
 * @author Jack
 * 
 */
public abstract class AbstractReachDeviationControl extends MotionPlaybackController {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractReachDeviationControl.class);

	Vector3f furthestHealthy, startHealthy, path;
	private float totalDistance;
	private List<TrackingFrame> mHealthy;
	private List<Vector3f> mHealthyV;

	private Mesh dparrow = new Line();
	private Mesh ddarrow = new Line();
	private Geometry debugProgArrow = new Geometry("dbgProgArrow", dparrow);
	private Geometry debugDevArrow = new Geometry("dbgDevArrow", ddarrow);
	private Quaternion offset = new Quaternion().fromAngleAxis(3 * FastMath.PI / 2, new Vector3f(0,
			1, 0));
	Material devMat;
	private float trajSensitivity = 1f, rotSensitivity = 1f;
	/**
	 * value which scales the deviation between healthy and subject. In practice
	 * controls difficulty of performing successful reach. Higher values are
	 * harder
	 **/
	private float difficulty = 1;

	public AbstractReachDeviationControl(String normTrackedObject, List<TrackingFrame> healthy,
			Node root, AssetManager assetManager, float trajSensitivity, float rotSensitivity)
			throws Exception {
		super(normTrackedObject, healthy);
		this.trajSensitivity = trajSensitivity;
		this.rotSensitivity = rotSensitivity;
		mHealthy = healthy;
		mHealthyV = new ArrayList<Vector3f>(mHealthy.size());
		for (TrackingFrame f : mHealthy) {
			TrackingObject o = f.getTrackingObjects().get(object);
			if (o != null)
				if (o.getObjectPoint() != null)
					mHealthyV.add(Utils.pFromFloat(o.getObjectPoint()));
		}
		Material progMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Material devMat = progMat.clone();
		progMat.setColor("Color", ColorRGBA.Yellow);
		devMat.setColor("Color", ColorRGBA.Red);
		debugDevArrow.setMaterial(devMat);
		debugProgArrow.setMaterial(progMat);
		root.attachChild(debugDevArrow);
		root.attachChild(debugProgArrow);
		startHealthy = mHealthyV.get(0);
		furthestHealthy = findFurthest(startHealthy, mHealthyV);
		totalDistance = startHealthy.distance(furthestHealthy);
		// debugProgArrow.setLocalScale(0);
	}

	public static Vector3f findFurthest(Vector3f from, List<Vector3f> mHealthyV2) {
		float dist = 0;
		Vector3f cur = null;
		for (Vector3f v : mHealthyV2) {
			if (from.distanceSquared(v) > dist) {
				LOG.trace("findFurthest found a further vector: " + v + " compared to current: "
						+ cur);
				dist = from.distanceSquared(v);
				cur = v;
			}
		}
		LOG.debug("findFurthest found furthest vector: " + cur);
		return cur;
	}

	public AbstractReachDeviationControl(String trackedObject, List<TrackingFrame> healthy,
			float difficulty, Node root, AssetManager assetManager, float transSensitivity,
			float rotSensitivity) throws Exception {
		this(trackedObject, healthy, root, assetManager, transSensitivity, rotSensitivity);
		this.difficulty = difficulty;
	}

	@Override
	protected void controlRender(RenderManager arg0, ViewPort arg1) {
		// TODO Auto-generated method stub

	}

	// private static void calcDeviation(TrackingFrame subj, List<TrackingFrame>
	// mData,

	@Override
	protected void controlUpdate(float tpf) {
		// super.controlUpdate(tpf);
		// normalize spatial to healthy position
		TrackingObject to = getSubjectTrackingObject();
		float[] p = to.getObjectPoint();
		if (p == null || p.length != 3) {
			LOG.debug("couldn't get subject point!");
			return;
		}
		Vector3f curPoint = new Vector3f(p[0], p[1], p[2]);
		int nearestIndex = findNearestIndex(curPoint, mHealthyV, object);
		Vector3f nextNearest;
		int nextNearestIndex;
		Vector3f healthyMotion;
		if (nearestIndex < (mHealthyV.size() - 1) && nearestIndex > 0) {
			nextNearestIndex = findNearestIndex(curPoint, mHealthyV, nearestIndex - 1,
					nearestIndex + 1);
		} else if (nearestIndex == mHealthyV.size() - 1) {
			nextNearestIndex = nearestIndex - 1;
		} else {
			nextNearestIndex = 0;
		}
		// calculate motion deviation from control path
		nextNearest = mHealthyV.get(nextNearestIndex);
		TrackingObject healthyTo = mHealthy.get(nearestIndex).getTrackingObjects().get(object);
		float[] npf = healthyTo.getObjectPoint();
		Vector3f nearestHealthy = Utils.pFromFloat(npf);
		if (nextNearestIndex < nearestIndex)
			healthyMotion = nextNearest.subtract(nearestHealthy);
		else
			healthyMotion = nearestHealthy.subtract(nextNearest);
		Vector3f trajDev = curPoint.subtract(nearestHealthy);
		Vector3f healthyComp = healthyMotion.mult(trajDev.dot(healthyMotion)
				/ healthyMotion.lengthSquared());
		trajDev = trajDev.subtract(healthyComp);
		path = furthestHealthy.subtract(startHealthy);
		trajDev = trajDev.mult(trajSensitivity);
		Quaternion healthyRot = Utils.qFromFloat(healthyTo.getObjectQuaternion());
		Quaternion subjRot = Utils.qFromFloat(to.getObjectQuaternion());
		// TODO: might need to flip these?
		Quaternion rotDev = subjRot.subtract(healthyRot);
		rotDev = rotDev.mult(rotSensitivity);
		// float completion = calculateCompletion(curPoint, path);
		// Vector3f prog = startHealthy.add(path.mult(completion));
		LOG.trace("nearest healthy point: " + nearestHealthy + " curPoint: " + curPoint
				+ " deviation: " + trajDev + " index: " + index);
		// dev = dev.add(prog);

		spatial.setLocalTranslation(nearestHealthy.add(healthyComp.add(trajDev)));
		//Quaternion q = Utils.qFromFloat(to.getObjectQuaternion());
		spatial.setLocalRotation(healthyRot.add(rotDev).mult(offset));
		//spatial.setLocalRotation(q.mult(offset).mult(rotSensitivity));
		if (Configuration.debug()) {
			Line devArrow = new Line(nearestHealthy, curPoint);
			debugDevArrow.setMesh(devArrow);
			// Line progArrow = new Line(start, prog);
			// debugProgArrow.setMesh(progArrow);

		}
	}

	public static float calculateCompletion(Vector3f cur, Vector3f path) {
		// Vector3f p = mHealthyV2.get(mHealthyV2.size() -
		// 1).subtract(mHealthyV2.get(0));
		float dot = path.dot(cur);
		return dot / path.lengthSquared();
		// return startHealthy.distance(cur) / totalDistance;
	}

	private int findNearestIndex(Vector3f ref, List<Vector3f> mHealthyV2, int i, int j) {
		return ref.distance(mHealthyV2.get(i)) < ref.distance(mHealthyV2.get(j)) ? i : j;
	}

	private Vector3f findNearestVector(Vector3f ref, Vector3f v1, Vector3f v2) {
		return ref.distance(v1) < ref.distance(v2) ? v1 : v2;
	}

	/**
	 * finds the point for the given trackingOjbect, in the given list, nearest
	 * to the given point.
	 * 
	 * @param point3f
	 * @param l
	 * @param to
	 */
	public static TrackingObject findNearestPointFrame(Vector3f p, List<TrackingFrame> l, String to) {
		TrackingObject r = null;
		float d = Float.MAX_VALUE;
		for (TrackingFrame f : l) {
			TrackingObject o = f.getTrackingObjects().get(to);
			if (o == null)
				continue;
			float[] arp = o.getObjectPoint();
			Vector3f v = new Vector3f(arp[0], arp[1], arp[2]);
			float tdist = p.distanceSquared(v);
			if (tdist < d) {
				// new point is closer
				r = o;
				d = tdist;
			}
		}
		return r;
	}

	private static int findNearestIndex(Vector3f p, List<Vector3f> l, String to) {
		int r = Integer.MAX_VALUE;
		float d = Float.MAX_VALUE;
		for (int i = 0; i < l.size(); i++) {
			Vector3f v = l.get(i);
			float tdist = p.distanceSquared(v);
			if (tdist < d) {
				// new point is closer
				r = i;
				d = tdist;
			}
		}
		return r;
	}

	/**
	 * subclasses override depending on the particular method by which they get
	 * subject motion data. (Live or recorded basically).
	 * 
	 * @return
	 */
	protected abstract TrackingObject getSubjectTrackingObject();

}
