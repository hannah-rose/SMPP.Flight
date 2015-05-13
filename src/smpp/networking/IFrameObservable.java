package smpp.networking;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.ric.smpp.domain.common.Common.TRACKING_DATA_TYPE;
import org.ric.smpp.domain.common.eventnotifications.Observable;
import org.ric.smpp.domain.core.interfaces.IFrameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.web.socket.WebSocketHttpHeaders;

/**
 * Observable that uses the {@link SimpleStompClient} to subscribe to
 * 
 * @author Jack
 * 
 */
public class IFrameObservable extends Observable<IFrameData> {
	private static final Logger LOG = LoggerFactory.getLogger(IFrameObservable.class);

	private List<String> dataTopics = new ArrayList<String>();
	private List<TRACKING_DATA_TYPE> dataTypes = new ArrayList<TRACKING_DATA_TYPE>();
	private MessageConverter mc = new MappingJackson2MessageConverter();
	private final MessageHandler mh = new MyMessageHandler();
	private String topicPrefix = "/topic/data/";
	final SimpleStompClient client;

	public IFrameObservable(URI uri, String un, String pwd, TRACKING_DATA_TYPE... types) {
		LOG.debug("constructing " + IFrameObservable.class.getName() + " with URI: " + uri
				+ " un: " + un + " pwd: " + pwd + " dataTopics: " + types);
		int i = 0;
		for (TRACKING_DATA_TYPE t : types) {
			dataTopics.add(topicPrefix + t);
			dataTypes.add(t);
			i++;
		}

		WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
		// loginAndSaveJsessionIdCookie(un, pwd, headers);
		client = new SimpleStompClient(uri, headers);
		try {
			client.connect(new MessageHandler() {

				public void handleMessage(Message<?> message) throws MessagingException {
					LOG.debug("maybe connected!");
					for (String topic : dataTopics)
						client.subscribe(topic, mh);
				}
				// TODO: should probably
				// check the message is positively confirming our connection. }
				// });
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isSubscribed(TRACKING_DATA_TYPE type) {
		for (String topic : dataTopics) {
			if (topic.equals(topicPrefix + type))
				return true;
		}
		return false;
	}

	public TRACKING_DATA_TYPE[] getObservedTypes() {
		return dataTypes.toArray(new TRACKING_DATA_TYPE[dataTypes.size()]);
	}

	/**
	 * call this method to have the TrackingFrameObservable subscribe to the
	 * topic for the given {@link TRACKING_DATA_TYPE}.
	 * 
	 * @param type
	 */
	public void subscribeTo(TRACKING_DATA_TYPE type) {
		if (!isSubscribed(type)) {
			LOG.debug(IFrameObservable.class.getName() + " not subscribed to TRACKING_DATA_TYPE: "
					+ type + ". subscribing..");
			client.subscribe(topicPrefix + type, mh);
			dataTypes.add(type);
			dataTopics.add(topicPrefix + type);
		} else {
			LOG.debug(IFrameObservable.class.getName()
					+ " already subscribed to TRACKING_DATA_TYPE: " + type);
		}
	}

	private class MyMessageHandler implements MessageHandler {
		@Override
		public void handleMessage(Message<?> arg0) throws MessagingException {
			LOG.trace("got subscriber message : " + arg0);
			IFrameData tf = (IFrameData) mc.fromMessage(arg0, IFrameData.class);
			if (tf != null)
				notifyObservers(tf);
			else
				LOG.info("recieved message, but couldn't be unmarshalled into TrackingFrame");
		}
	}

	/**
	 * closes and releases resources (network resources in particular) acquired
	 * by this {@link IFrameObservable}
	 * @throws Exception 
	 */
	public void close() throws Exception {
		client.close();
	}

}