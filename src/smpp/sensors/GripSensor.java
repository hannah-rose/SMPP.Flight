package smpp.sensors;

import org.ric.smpp.domain.common.Common.TRACKING_DATA_TYPE;
import org.ric.smpp.domain.common.eventnotifications.Observable;
import org.ric.smpp.domain.common.eventnotifications.Observer;
import org.ric.smpp.domain.core.interfaces.IFrameData;
import org.ric.smpp.domain.serialclient.SerialSensorFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smpp.sensors.interfaces.GripListener;

public class GripSensor extends TaskCompletionTracker<GripListener, IFrameData> implements Observer<IFrameData> {
	public GripSensor(Observable<IFrameData> o, GripListener... l) {
		super(o, l);
	}

	private static final Logger LOG = LoggerFactory.getLogger(GripSensor.class);
	protected final float graspThreshold = 10;

	@Override
	public void update(Observable<IFrameData> object, IFrameData data) {
		if (data instanceof SerialSensorFrame) {
			if (data.getFrameSource().equals(TRACKING_DATA_TYPE.SerialObject)) {
				SerialSensorFrame ssf = (SerialSensorFrame) data;
				float[] serialVals = ssf.getSensorValues();
				boolean completed = false;
				int row = 8;
				int dif = 3;
				LOG.trace("checking for task completion");
				for (int i = 0; i < serialVals.length; i += row) {
					for (int k = 0; k < row; k++) {
						int roof = Math.min(i + k + (row - dif) + 1, i + row);
						LOG.trace("k: " + k + " i: " + i + " roof: " + roof);
						for (int j = i + k + dif; j < roof; j++) {
							LOG.trace("checking serial value " + (i + (j % row)) + " and "
									+ (i + k));
							if (serialVals[i + (j % row)] > graspThreshold
									&& serialVals[i + k] > graspThreshold) {
								LOG.debug("grip task completed!");
								completed = true;
							}
						}
					}
				}
				if (completed != mTaskCompleted)
					setTaskCompleted(completed);
			}
		}
	}

	@Override
	protected void notifyListeners(Object newData) {
		LOG.debug("notifying listeners of new grip state: " + newData);
		for(GripListener l : listeners)
			l.onGripChange((boolean)newData);
	}

}
