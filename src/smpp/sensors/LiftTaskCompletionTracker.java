package smpp.sensors;

import org.ric.smpp.domain.common.eventnotifications.Observable;
import org.ric.smpp.domain.core.interfaces.IFrameData;
import org.ric.smpp.domain.serialclient.SerialSensorFrame;

import smpp.sensors.interfaces.GripListener;

public class LiftTaskCompletionTracker extends TaskCompletionTracker<GripListener, IFrameData> {

	public LiftTaskCompletionTracker(Observable<IFrameData> o){
		super(o);
	}
	public LiftTaskCompletionTracker(Observable<IFrameData> o, GripListener... l) {
		super(o, l);
	}

	@Override
	public void update(Observable<IFrameData> object, IFrameData data) {
		if (data instanceof SerialSensorFrame) {
			SerialSensorFrame ssf = (SerialSensorFrame) data;
			float[] serialVals = ssf.getSensorValues();
			int row = 8;
			int dif = 3;
			LOG.trace("checking for task completion");
			for (int i = 0; i < serialVals.length; i += row) {
				for (int k = 0; k < row; k++) {
					int roof = Math.min(i + k + (row - dif) + 1, i + row);
					LOG.trace("k: " + k + " i: " + i + " roof: " + roof);
					for (int j = i + k + dif; j < roof; j++) {
						LOG.trace("checking serial value " + (i + (j % row)) + " and " + (i + k));
						if (serialVals[i + (j % row)] > graspThreshold
								&& serialVals[i + k] > graspThreshold) {
							LOG.debug("task completed!");
							setTaskCompleted(true);
						}
					}
				}
			}
		}
	}

	@Override
	protected void notifyListeners(Object newData) {
		for(GripListener l : listeners)
			l.onGripChange((boolean)newData);
	}
}

