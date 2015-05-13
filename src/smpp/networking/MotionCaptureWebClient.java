package smpp.networking;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.ric.smpp.domain.Experiment;
import org.ric.smpp.domain.Session;
import org.ric.smpp.domain.Trial;
import org.ric.smpp.domain.common.Common.TRACKING_DATA_TYPE;
import org.ric.smpp.domain.core.TrackingFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MotionCaptureWebClient {
	private final static Logger LOG = LoggerFactory.getLogger(MotionCaptureWebClient.class);
	public final static String SpringISODateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	URI mHost;
	//URIBuilder mHostBuilder;
	// TODO: refactor paths out of SMPPMotionWeb and here. so they can both
	// reference same values
	static final String AcceptHeaderKey = "Accept";
	static final String ApplicationJson = "application/json";
	static final String experimentsPath = "/experiment/";
	static final String sessionsPath = "/session/";
	static final String trialsPath = "/trial/";
	static final String dataPath = "/data/json";
	static final String createTrialPath = "/createTrial";
	static final String createExperimentPath = "/newexperiment";
	static final String createSessionPath = "/createSession";

	ObjectMapper mapper = new ObjectMapper();
	CloseableHttpClient client = HttpClients.createDefault();
	HttpContext localContext = new BasicHttpContext();
	CookieStore cookieStore = new BasicCookieStore();

	public MotionCaptureWebClient(URI host) {
		mHost = host;
		//mHostBuilder = new URIBuilder(mHost);
		localContext.setAttribute(ClientContext.COOKIE_SPEC, cookieStore);
	}

	private String experimentKey = "experimentList";

	public List<Experiment> getExperiments() throws URISyntaxException, JsonProcessingException,
			IOException {
		List<Experiment> experiments = null;
		URI uri = new URIBuilder(mHost).setPath(experimentsPath).build();
		LOG.debug("getExperiments. uri: " + uri);
		URLConnection conn = getApplicationJsonUrlConnection(uri);
		JsonNode n = mapper.readTree(conn.getInputStream());
		experiments = mapper.readValue(n.get(experimentKey).toString(),
				new TypeReference<List<Experiment>>() {
				});
		return experiments;
	}

	public Set<Session> getSessions(Experiment e) throws JsonProcessingException,
			URISyntaxException, IOException {
		return getSessions(e.getId());
	}

	public UUID createExperiment(String name) throws URISyntaxException, ClientProtocolException, IOException{
		URI uri = new URIBuilder(mHost).setPath(createExperimentPath).build();
		HttpPost post = new HttpPost(uri);
		List<NameValuePair> formVals = new ArrayList<NameValuePair>();
		formVals.add(new BasicNameValuePair("experimentName", name));
		post.setEntity(new UrlEncodedFormEntity(formVals));
		return client.execute(post, redirectResponseHandler, localContext);
	}

	public UUID createSession(Experiment e, String name) throws ClientProtocolException, URISyntaxException, IOException {
		return createSession(UUID.fromString(e.getId()), name);
	}

	public UUID createSession(UUID exp, String name) throws URISyntaxException, ClientProtocolException, IOException {
		URI uri = new URIBuilder(mHost).setPath(createSessionPath).addParameter("experimentId", exp.toString()).build();
		HttpPost post = new HttpPost(uri);
		List<NameValuePair> formVals = new ArrayList<NameValuePair>();
		formVals.add(new BasicNameValuePair("sessionName", name));
		post.setEntity(new UrlEncodedFormEntity(formVals));
		return client.execute(post, redirectResponseHandler, localContext);
	}

	public Set<Session> getSessions(String experimentId) throws JsonProcessingException,
			URISyntaxException, IOException {
		Set<Session> sessions = null;
		Experiment e = getExperiment(experimentId);
		return e.getSessions();
	}

	private Experiment getExperiment(String id) throws JsonParseException, JsonMappingException,
			IOException, URISyntaxException {
		Experiment e = getObject(experimentsPath + id + '/', new TypeReference<Experiment>() {
		}, "experiment");
		return e;
	}

	public List<Session> getSessions() throws MalformedURLException, IOException,
			URISyntaxException {
		List<Session> sessions = getDataList(sessionsPath);
		LOG.debug("sessions: " + sessions);
		return sessions;
	}

	/**
	 * not sure what to do with this yet.
	 * 
	 * @param path
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public <T> List<T> getDataList(String path) throws MalformedURLException, IOException,
			URISyntaxException {
		List<T> objs = null;
		URI uri = new URIBuilder(mHost).setPath(path).build();
		URLConnection conn = getApplicationJsonUrlConnection(uri);
		objs = mapper.readValue(conn.getInputStream(), new TypeReference<List<T>>() {
		});
		return objs;
	}

	private String trialKey = "trial";

	public Trial getTrial(String id) throws URISyntaxException, MalformedURLException, IOException {
		Trial t = null;
		URI uri = new URIBuilder(mHost).setPath(trialsPath + id + '/').build();
		LOG.debug("getTrial. uri: " + uri);
		URLConnection conn = getApplicationJsonUrlConnection(uri);
		JsonNode n = mapper.readTree(conn.getInputStream());
		t = mapper.treeToValue(n.get(trialKey), Trial.class);
		return t;
	}

	private <T> T getObject(String path, TypeReference<T> t, String key) throws JsonParseException,
			JsonMappingException, IOException, URISyntaxException {
		T obj = null;
		JsonNode root;
		URI uri = new URIBuilder(mHost).setPath(path).build();
		URLConnection conn = getApplicationJsonUrlConnection(uri);
		if (key != null) {
			obj = mapper.readValue(mapper.readTree(conn.getInputStream()).get(key).toString(), t);
		} else
			obj = mapper.readValue(conn.getInputStream(), t);
		return obj;
	}

	private URLConnection getApplicationJsonUrlConnection(URI uri) throws MalformedURLException,
			IOException {
		URLConnection conn = uri.toURL().openConnection();
		conn.addRequestProperty(AcceptHeaderKey, ApplicationJson);
		return conn;
	}

	public <T> List<T> getData(TRACKING_DATA_TYPE type, Trial t) throws IOException,
			URISyntaxException {
		List<T> data = getData(type, t.getStartTime().getTime(), t.getEndTime().getTime());
		return data;
	}

	public <T> List<T> getData(TRACKING_DATA_TYPE type, long startTime, long endTime)
			throws IOException, URISyntaxException {
		List<T> data = null;
		URI uri = new URIBuilder(mHost).setPath(dataPath)
				.addParameter("startTime", formatDate(startTime))
				.addParameter("endTime", formatDate(endTime))
				.addParameter("sensorType", type.toString()).build();
		LOG.debug("getData. uri: " + uri + '\n' + "url: " + uri.toURL());
		URLConnection conn = uri.toURL().openConnection();
		conn.addRequestProperty("Accept", "application/json");
		InputStream in = conn.getInputStream();

		data = mapper.readValue(in, new TypeReference<List<TrackingFrame>>() {
		});
		return data;
	}

	ResponseHandler<UUID> redirectResponseHandler = new ResponseHandler<UUID>() {

		@Override
		public UUID handleResponse(HttpResponse resp) throws ClientProtocolException, IOException {
			if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
				LOG.warn("error handling response. Expected response code: "
						+ HttpStatus.SC_MOVED_TEMPORARILY + " was: "
						+ resp.getStatusLine().getStatusCode());
				return null;
			}
			String location = resp.getFirstHeader(HttpHeaders.LOCATION).getValue().split(";")[0];
			LOG.debug("Location: " + location);
			String[] path = location.split("/");

			return UUID.fromString(path[path.length - 1]);
		}
	};

	public UUID startRecording(UUID sessionId, TRACKING_DATA_TYPE... recordedSensorTypes)
			throws MalformedURLException, URISyntaxException, IOException {
		Set<TRACKING_DATA_TYPE> t = new HashSet<TRACKING_DATA_TYPE>(
				Arrays.asList(recordedSensorTypes));
		return startRecording(sessionId, t);
	}

	public UUID startRecording(UUID sessionId, Set<TRACKING_DATA_TYPE> dataTypes)
			throws URISyntaxException, MalformedURLException, IOException {

		URI uri = new URIBuilder(mHost).setPath(createTrialPath)
				.addParameter("sessionId", sessionId.toString()).build();
		HttpPost post = new HttpPost(uri);
		post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		for (TRACKING_DATA_TYPE t : dataTypes) {
			urlParameters.add(new BasicNameValuePair("sensors", t.toString()));
			urlParameters.add(new BasicNameValuePair("_sensors", "on"));
		}
		post.setEntity(new UrlEncodedFormEntity(urlParameters));
		return client.execute(post, redirectResponseHandler, localContext);
	}

	public void stopRecording(UUID trialId) throws MalformedURLException, URISyntaxException,
			IOException {
		Trial t = getTrial(trialId.toString());
		stopRecording(UUID.fromString(t.getId()), t.getSensorTypes());
	}

	private void stopRecording(UUID trialId, Set<TRACKING_DATA_TYPE> types)
			throws URISyntaxException, ClientProtocolException, IOException {
		LOG.info("stopping recording for trial id: " + trialId);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (TRACKING_DATA_TYPE t : types)
			nvps.add(new BasicNameValuePair("sensorTypes", t.toString()));
		nvps.add(new BasicNameValuePair("trialId", trialId.toString()));
		URI uri = new URIBuilder(mHost).setPath("/stopRecording").addParameters(nvps).build();
		HttpGet get = new HttpGet(uri);
		client.execute(get, localContext).close();
	}

	private static String formatDate(long d) {
		return DateFormatUtils.format(d, SpringISODateFormat);
	}

	private static String formatDate(Date d) {
		return DateFormatUtils.format(d, SpringISODateFormat);
	}
}
