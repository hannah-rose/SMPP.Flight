package smpp.sensors;

import java.util.Arrays;

import org.ric.smpp.domain.common.eventnotifications.Observable;
import org.ric.smpp.domain.common.eventnotifications.Observer;
import org.ric.smpp.domain.core.interfaces.IFrameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple class. Determines if a 'task' is completed. Does this by monitoring
 * {@link IFrameData} updates. Subclasses implement task specific monitor,
 * criteria, override
 * {@link #update(org.ric.smpp.domain.common.eventnotifications.Observable, Object)}
 * 
 * @author Jack
 * @param <T>
 *            the type of listener object.
 * 
 */
public abstract class TaskCompletionTracker<T, K> extends SensorManager<T> implements
		Observer<K> {
	protected static final Logger LOG = LoggerFactory.getLogger(TaskCompletionTracker.class);

	protected boolean mTaskCompleted = false;

	protected final float graspThreshold = 10;

	public TaskCompletionTracker(Observable<K> o) {
		o.addObserver(this);
	}

	public TaskCompletionTracker(Observable<K> o, T... l) {
		this(o);
		listeners.addAll(Arrays.asList(l));
	}

	public boolean isTaskCompleted() {
		return mTaskCompleted;
	}

	public void setTaskCompleted(boolean v) {
		mTaskCompleted = v;
		notifyListeners(v);
	}

	/**
	 * Subclasses implementing behavior for particular sensor and task types
	 * must implement this. notify listeners of the event by calling the
	 * callback particular to the listener type.
	 * 
	 * @return
	 */
	protected abstract void notifyListeners(Object newData);
}
