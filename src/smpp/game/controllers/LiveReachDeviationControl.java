package smpp.game.controllers;

import java.util.List;

import org.ric.smpp.domain.common.eventnotifications.Observable;
import org.ric.smpp.domain.common.eventnotifications.Observer;
import org.ric.smpp.domain.core.TrackingFrame;
import org.ric.smpp.domain.core.TrackingObject;
import org.ric.smpp.domain.core.interfaces.IFrameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smpp.configuration.Configuration;
import smpp.networking.IFrameObservable;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

/**
 * implementation of {@link AbstractReachDeviationControl} which uses live frame
 * data from an {@link Observable}. 
 * 
 * @author HAMRR
 * 
 */
public class LiveReachDeviationControl extends AbstractReachDeviationControl implements
		Observer<IFrameData> {
	private static Logger LOG = LoggerFactory.getLogger(LiveReachDeviationControl.class);
	private TrackingObject mostRecentFrame = new TrackingObject();
	private String trackedObject;

	public LiveReachDeviationControl(IFrameObservable o, String trackedObject, String normTrackedObject, List<TrackingFrame> healthy,
			float difficulty, Node root, AssetManager assetManager, float ts, float rs) throws Exception {
		super(normTrackedObject, healthy, difficulty, root, assetManager, ts, rs);
		this.trackedObject = trackedObject;
		o.addObserver(this);
	}

	@Override
	protected TrackingObject getSubjectTrackingObject() {
		return mostRecentFrame;
	}

	@Override
	public void update(Observable<IFrameData> object, IFrameData data) {
		if (data instanceof TrackingFrame
				&& data.getFrameSource().equals(Configuration.getNormTrackedDataType())) {
			TrackingFrame f = (TrackingFrame) data;
			TrackingObject o;
			if ((o = f.getTrackingObjects().get(trackedObject)) != null) {
				LOG.trace("new TrackingFrame with desired TrackingObject (" + trackedObject + ")");
				mostRecentFrame = o;
			}
		}

	}

}
