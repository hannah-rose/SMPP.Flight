package smpp.game.control;

import org.ric.smpp.domain.core.TrackingFrame;

public interface TrackingFrameExtractor<T, K extends TrackingFrame> {

	public boolean canExtract(K tf);
	public T extract(K tf);
}
