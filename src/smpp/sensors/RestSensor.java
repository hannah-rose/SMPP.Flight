package smpp.sensors;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import org.ric.smpp.domain.common.Common.TRACKING_DATA_TYPE;
import org.ric.smpp.domain.common.eventnotifications.Observable;
import org.ric.smpp.domain.common.eventnotifications.Observer;
import org.ric.smpp.domain.core.interfaces.IFrameData;
import org.ric.smpp.domain.serialclient.SerialSensorFrame;

import smpp.sensors.interfaces.RestListener;

public class RestSensor extends SensorManager<RestListener> implements Observer<IFrameData> {

	private boolean lastState = false;
	Deque<Float> sensValues = new LinkedList<Float>();
	private static final int LENGTH = 50;
	private static final float avg_threshold = 0.5f;
	public RestSensor(Observable<IFrameData> o){
		o.addObserver(this);
	}
	@Override
	public void update(Observable<IFrameData> object, IFrameData data) {
		if (data instanceof SerialSensorFrame) {
			SerialSensorFrame f = (SerialSensorFrame) data;
			if (f.getFrameSource().equals(TRACKING_DATA_TYPE.SerialRestPad)) {
				float v = -1;
				if(f.getSensorValues().length > 2)
					v = f.getSensorValues()[2];
				modifyList(sensValues, v);
				boolean newState = isResting(sensValues);
				if (newState != lastState)
					notifyListeners(newState);
				lastState = newState;
			}
		}

	}

	private void modifyList(Deque<Float> values, float v) {
		if (values.size() < LENGTH) {
			values.add(v);
		} else {
			values.poll();
			values.add(v);
		}
	}

	private static boolean isResting(Collection<Float> floats) {
		float count = 0;
		for (float f : floats) {
			count += f;
		}
		float avg = count / LENGTH;
		return avg > avg_threshold;
	}

	private void notifyListeners(boolean newState) {
		for (RestListener rl : listeners)
			rl.onRestChange(newState);
	}

}
