package smpp.sensors;

import java.util.ArrayList;
import java.util.List;

import smpp.sensors.interfaces.GripListener;

public abstract class SensorManager<T> {

	List<T> listeners = new ArrayList<T>();

	public final void registerListener(T l) {
		listeners.add(l);
	}

	public final void removeListener(GripListener l) {
		listeners.remove(l);
	}


}
