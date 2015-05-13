package smpp.game;

import static smpp.configuration.Configuration.getSMPPServicePW;
import static smpp.configuration.Configuration.getSMPPServiceUN;
import static smpp.configuration.Configuration.getSMPPWebSocketServiceURI;
import static smpp.configuration.Configuration.getTrackedType;

import java.util.ArrayList;
import java.util.List;

import org.ric.smpp.domain.core.TrackingFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smpp.networking.IFrameObservable;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;

public class Utils {
	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

	/**
	 * builds {@link Vector3f} from given float
	 * 
	 * @param v
	 * @return
	 */
	public static Vector3f pFromFloat(float[] v) {
		if (v.length != 3)
			return null;
		return new Vector3f(v[0], v[1], v[2]);
	}

	public static Quaternion qFromFloat(float[] v) {
		if (v.length != 4)
			return null;
		return new Quaternion(v[0], v[1], v[2], v[3]);
	}

	public static void setText(Screen s, String elementId, String msg) {
		LOG.debug("Screen: " + s + " elementId: " + elementId + " msg: " + msg);
		Element e = s.findElementByName(elementId);
		TextRenderer tr = e.getRenderer(TextRenderer.class);
		tr.setText(msg);
		int width = tr.getTextWidth();
		// e.getParent().layoutElements();
		e.setConstraintWidth(new SizeValue(width + "px"));
		e.setWidth(width);
		// e.getParent().layoutElements();
		s.layoutLayers();
		// e.layoutElements();
	}

	private static IFrameObservable obs;

	public static IFrameObservable getIFrameObservable() {
		if (obs == null) {
			obs = new IFrameObservable(getSMPPWebSocketServiceURI(), getSMPPServiceUN(),
					getSMPPServicePW(), getTrackedType());
		}
		return obs;
	}

	//BROKEN
//	public static float calculateDistanceToFrame(List<TrackingFrame> frames, String trObject,
//			TrackingFrame target) {
//		float dist = 0;
//		if (frames.size() == 0)
//			return 0;
//		TrackingFrame cur = frames.get(0);
//		int i = 0;
//		while (!cur.equals(target) && i < frames.size()) {
//			cur = frames.get(i);
//			i++;
//			if (cur.getTrackingObjects().get(trObject) != null) {
//				TrackingObject tO = cur.getTrackingObjects().get(target);
//				dist += Utils.fromFloat(tO.getObjectPoint()).length();
//			}
//		}
//		return dist;
//	}

	public static float calculateDistance(List<TrackingFrame> frames, String to) {
		return calculateDistance(toVectors(frames, to));
	}

	public static float calculateDistance(List<Vector3f> vs) {
		float dist = 0;
		Vector3f prev = null;
		for (Vector3f v : vs) {
			if (prev == null) {
				prev = v;
				continue;
			}
			dist += v.subtract(prev).length();
			prev = v;
		}

		return dist;
	}

	public static float calculatePathDistanceTo(List<Vector3f> vs, Vector3f target) {
		float dist = 0;
		Vector3f prev = null;
		for (Vector3f v : vs) {
			if (prev == null) {
				prev = v;
				continue;
			}
			if (v.equals(target))
				return dist;
			dist += v.subtract(prev).length();
			prev = v;
		}
		return dist;
	}

	public static List<Vector3f> toVectors(List<TrackingFrame> frames, String obj) {
		List<Vector3f> vs = new ArrayList<Vector3f>(frames.size());
		for (TrackingFrame f : frames) {
			if (f.getTrackingObjects().containsKey(obj)) {
				vs.add(Utils.pFromFloat(f.getTrackingObjects().get(obj).getObjectPoint()));
			}
		}
		return vs;
	}
}
