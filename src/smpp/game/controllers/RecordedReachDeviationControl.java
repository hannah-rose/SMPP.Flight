package smpp.game.controllers;

import java.util.List;

import org.ric.smpp.domain.core.TrackingFrame;
import org.ric.smpp.domain.core.TrackingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

public class RecordedReachDeviationControl extends AbstractReachDeviationControl {
	private static final Logger LOG = LoggerFactory.getLogger(RecordedReachDeviationControl.class);
	
	private List<TrackingFrame> mSubject;
	public RecordedReachDeviationControl(String trackedObject, List<TrackingFrame> healthy,
			List<TrackingFrame> subject, Node root, AssetManager assetManager, float transSensitivity, float rotSensitivity) throws Exception {
		super(trackedObject, healthy, root, assetManager, transSensitivity, rotSensitivity);
		mSubject = subject;
	}

	/**
	 * finds {@link TrackingObject} from recorded subject tracking data list
	 * 
	 * @return
	 */
	protected TrackingObject getSubjectTrackingObject() {
		TrackingFrame tf = mSubject.get(currentTrackingFrameIndex(motion, sTime, index));
		if (tf == null) {
			LOG.info("current frame is null");
			return null;
		}
		TrackingObject to = tf.getTrackingObjects().get(object);
		return to;
	}
}
