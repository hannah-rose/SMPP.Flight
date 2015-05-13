package smpp.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import org.ric.smpp.domain.common.Common.TRACKING_DATA_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;



/**
 * Static class for accessing configuration options for SMPPMotion game. Values
 * are loaded from file named {@link #configName} in working directory. All
 * configuration values are accessible via static accessor methods on this
 * class.
 * 
 * @author Jack
 * 
 */
public class Configuration {
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    private static final String configName = "config.yaml";

    private static final String kTrackedType = "TrackedType";
    private static final String kTrackedObject = "TrackedObject";
    private static final String kSMPPWebSocketServiceURI = "SMPPMotionWebSocketURI";
    private static final String kSMPPMotionWebURI = "SMPPMotionWebURI";
    private static final String kSMPPServiceUN = "SMPPMotionWebUN";
    private static final String kSMPPServicePW = "SMPPMotionWebPW";
    private static final String kNormTrackedObject = "NormTrackedObject";
    private static final String kNormTrackedType = "NormTrackedType";
    private static final String kNormExperimentId = "NormExperimentID";
    private static final String kDebug = "Debug";

    private static TRACKING_DATA_TYPE[] trackedTypes;
    private static String trackedObject;
    private static String normTrackedObject;
    private static TRACKING_DATA_TYPE normTrackedType;
    private static URI SMPPWebSocketServiceURI;
    private static URI SMPPWebServiceURI;
    private static String SMPPServiceUN;
    private static String SMPPServicePW;
    private static String normExperimentID;
    private static String userId;
    private static boolean debug = false;

    private static Map<String, Object> configMap;


    /**
     * initializes configuration values to defaults or values from config file.
     * MUST be called before calling any accessor methods
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @SuppressWarnings("unchecked")
	public static void init() throws IOException, URISyntaxException {
        //initialize defaults
        SMPPWebSocketServiceURI = new URI("ws://localhost:8888/datastream/websocket");
        SMPPWebServiceURI = new URI("http://localhost:8888/");
        SMPPServiceUN = "admin";
        SMPPServicePW = "juniormint";
        trackedTypes = new TRACKING_DATA_TYPE[] {TRACKING_DATA_TYPE.ThreeGearData};
        normTrackedType = TRACKING_DATA_TYPE.ThreeGearData;
        normTrackedObject = "RIGHT_HAND_1";
        trackedObject = "RIGHT_HAND_1";


        File configFile = new File(configName);
        if (configFile.createNewFile()) {
            // file didn't exist
            // TODO: construct default config file
            LOG.info("no yaml config file found (expecting " + configFile.getAbsolutePath() + ") generating default");
            return;
        }
        // file exists. load config from yaml
        // Yaml y = new Yaml(new Constructor(Configuration.class));
        Constructor constructor = new PropertyConstructor();
        Yaml y = new Yaml(constructor);
        configMap = (Map<String, Object>) y.load(new FileInputStream(configFile));
        LOG.info("parsed yaml config file at " + configFile.getAbsolutePath() + "\n" + "configurations: " + configMap);

        //assign config values from parsed yaml
        ArrayList<TRACKING_DATA_TYPE> ar = (ArrayList<TRACKING_DATA_TYPE>) configMap.get(kTrackedType);
        trackedTypes = ar.toArray(new TRACKING_DATA_TYPE[ar.size()]);
        trackedObject = (String) configMap.get(kTrackedObject);
        normTrackedObject = (String) configMap.get(kNormTrackedObject);
        normTrackedType = (TRACKING_DATA_TYPE) configMap.get(kNormTrackedType);
        SMPPWebSocketServiceURI = (URI) configMap.get(kSMPPWebSocketServiceURI);
        SMPPWebServiceURI = (URI) configMap.get(kSMPPMotionWebURI);
        SMPPServiceUN = (String) configMap.get(kSMPPServiceUN);
        SMPPServicePW = (String) configMap.get(kSMPPServicePW);
        normExperimentID = (String) configMap.get(kNormExperimentId);

        debug = (Boolean) configMap.get(kDebug);
    }


    /**
     * @return the trackedType
     */
    public static TRACKING_DATA_TYPE[] getTrackedType() {
    	return trackedTypes;
        //return new TRACKING_DATA_TYPE[]{ trackedType};
    }


    /**
     * @return the sMPPServiceURI
     */
    public static URI getSMPPWebSocketServiceURI() {
        return SMPPWebSocketServiceURI;
    }
    public static URI getSMPPWebServiceURI(){
    	return SMPPWebServiceURI;
    }

    /**
     * @return the sMPPServiceUN
     */
    public static String getSMPPServiceUN() {
        return SMPPServiceUN;
    }


    /**
     * @return the sMPPServicePW
     */
    public static String getSMPPServicePW() {
        return SMPPServicePW;
    }


    public static String getTrackedObject() {
        return trackedObject;
    }

    public static TRACKING_DATA_TYPE getNormTrackedDataType(){
    	return normTrackedType;
    }
    
    public static String getNormTrackedObject(){
    	return normTrackedObject;
    }
    public static String getNormExperimentID(){
    	return normExperimentID;
    }
    public static String getUserId(){
    	return userId;
    }
    public static void setUserId(String id){
    	userId = id;
    }


    /** whether debug is set to true in configuration
     * 
     * @return value of debug flag in configuration
     */
	public static boolean debug() {
		return debug;
	}
	public static boolean chaseCam(){
		return (Boolean) configMap.get("chaseCam");
	}
}
