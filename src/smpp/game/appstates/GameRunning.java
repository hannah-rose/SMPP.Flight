/*
 * Initializes the game world and controls its level progression
 * Has several subordinate classes and states
 */
package smpp.game.appstates;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.ric.smpp.domain.Trial;
import org.ric.smpp.domain.common.Common.TRACKING_DATA_TYPE;
import org.ric.smpp.domain.core.TrackingFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.scene.Node;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.Application;
import com.jme3.input.InputManager;
import com.jme3.asset.AssetManager;
import com.jme3.renderer.ViewPort;
import com.jme3.input.FlyByCamera;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;
import com.jme3.app.SimpleApplication;

import smpp.configuration.Configuration;
import smpp.configuration.GameConfiguration;
import smpp.configuration.SetConfig;
import smpp.game.*;
import smpp.game.control.*;
import smpp.game.display.*;
import smpp.game.effects.*;
import smpp.networking.MotionCaptureWebClient;
import smpp.networking.IFrameObservable;
import smpp.sensors.RestSensor;


/**
 * Appstate for running game
 * @author Hannah
 */
public class GameRunning extends AbstractAppState {
    //Access game components
    private SimpleApplication app;
    private InputManager inputManager;
    private AssetManager assetManager;
    private Node rootNode;
    private ViewPort viewPort;
    private Camera cam;
    private FlyByCamera flyCam;
    private AppSettings settings;
    private static final Logger LOG = LoggerFactory.getLogger(GameRunning.class);
    
    // Variables from Jack's GamePlayAppState
    // TODO: clean up with comments once they are all integrated
    private List<TrackingFrame> healthyMotion;
    private UUID experimentId, setId, trialId;
    private IFrameObservable o;
    
    List<SetConfig> setConfigurations;
    //float sensitivity = 0.1f;
    MotionCaptureWebClient mMcwc;
    String nTrackedObject = Configuration.getNormTrackedObject();
    SetConfig curSetConfig;
    
    /*
     * Variable to set control source for game
     * TODO: set from start screen
     */
    enum Control {
    	USER, PLAYBACK, KEYS
    }
    
    //private boolean isReaching = false;
    
    //*********************************************************
    
    //Constructor
    public GameRunning(Node rootNode, AssetManager assetManager, ViewPort viewPort, InputManager inputManager,
                       AppStateManager stateManager, Camera cam, FlyByCamera flyCam ){
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.viewPort = viewPort;
        this.inputManager = inputManager;
        this.cam = cam;
        this.flyCam = flyCam;
    }
    
    World world = new World(rootNode, assetManager, viewPort);
    InitializeGame flightgame = new InitializeGame(rootNode, inputManager, assetManager);
    Key_Motion key_motion;
    Status game_status;
    GamePhysics physics;
    Sound sound;
    Control control = Control.USER;
    
    @Override
    public void update(float tpf){
        
    }
    
    @Override
    public void cleanup() {
    	
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.inputManager = this.app.getInputManager();
        this.assetManager = this.app.getAssetManager();
        this.viewPort = this.app.getViewPort();
        this.flyCam = this.app.getFlyByCamera();
        this.cam = this.app.getCamera();
        
        world.draw_world(assetManager, viewPort, rootNode);
        flightgame.initialize_game(cam, flyCam, rootNode, inputManager, assetManager, world.platforms, stateManager);
        
        game_status = new Status(assetManager,settings);
        sound = new Sound(assetManager);
        physics = new GamePhysics(stateManager, world, flightgame.player, game_status, sound);
        
        // Test get data
        //TODO: do I need the commented-out stuff
        try {
        	mMcwc = new MotionCaptureWebClient(Configuration.getSMPPWebServiceURI());
        	// Trial t = mMcwc.getTrial(Configuration.getNormExperimentID());
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        
        // More configurations
        // TODO: 
        try {
        	setConfigurations = GameConfiguration.getSetListForId(Configuration.getUserId());
        	int numSets = setConfigurations.size();
        }
        catch (IOException e) {
        	// couldn't find sesion configuration for configured userId
        	e.printStackTrace();
        }
        
        // Initialize control
        switch(control) {
        case USER:
        	// Live motion tracking
        	try {
        		UserMotionControl userMotionControl = new UserMotionControl(Configuration.getTrackedObject());
        		flightgame.player.addControl(userMotionControl);
        	} catch (Exception e1) {
        		e1.printStackTrace();
        	}
        case PLAYBACK:
        	// Playback control of a recorded reach
        	try {
    			MotionPlaybackController playbackControl = new MotionPlaybackController("RIGHT_HAND_1", healthyMotion);
    			flightgame.player.addControl(playbackControl);
    			LOG.debug("Motion control activated");
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
        case KEYS:
        	// Use the keyboard to control the plane
        	try {
        		key_motion = new Key_Motion(flightgame.player, cam);
        		stateManager.attach(key_motion);
        	} catch (Exception e1) {
    			e1.printStackTrace();
    		}
        }
        
        
        // Attach states
        stateManager.attach(game_status);
        stateManager.attach(physics);
        stateManager.attach(sound);
    }
}

/*private void initSetParams(int set) throws ClientProtocolException, URISyntaxException, IOException 
{
	curSetConfig = setConfigurations.get(set);
	game_status.num_reps = curSetConfig.getNumTrials();
	int numSets = setConfigurations != null ? setConfigurations.size() : 0;
	if (curSetConfig.getExpId() != null)
		experimentId = UUID.fromString(curSetConfig.getExpId());
	else {
		experimentId = mMcwc.createExperiment(Configuration.getUserId() + "_exp");
		curSetConfig.setExpId(experimentId.toString());
		GameConfiguration.saveGameConfiguration();
	}
	setId = mMcwc.createSession(experimentId, "set_" + set);
	Trial t = mMcwc.getTrial(curSetConfig.getNormId());
	healthyMotion = mMcwc.getData(TRACKING_DATA_TYPE.ThreeGearData, t.getStartTime().getTime(),
			t.getEndTime().getTime());
	LOG.info(GameRunning.class + " initialized. trial " + game_status.rep + "/" + game_status.num_reps
			+ ", set " + set + "/" + numSets + " curSetConfig: " + curSetConfig
			+ " experimentId: " + experimentId + " normTrialId: " + t.getId());
}
}
*/

/*private TrackingFrameExtractor<Vector3f, TrackingFrame> trackedObjPositionExtractor = new TrackingFrameExtractor<Vector3f, TrackingFrame>() {
	@Override
	public Vector3f extract(TrackingFrame tf) {
		if (canExtract(tf))
			return Utils.pFromFloat(tf.getTrackingObjects().get(nTrackedObject)
					.getObjectPoint());
		return null;
	}

	@Override
	public boolean canExtract(TrackingFrame tf) {
		return tf.getTrackingObjects().containsKey(nTrackedObject);
	}
};
private TrackingFrameExtractor<Quaternion, TrackingFrame> trackedObjQuatExtractor = new TrackingFrameExtractor<Quaternion, TrackingFrame>() {
	@Override
	public Quaternion extract(TrackingFrame tf) {
		if (canExtract(tf))
			return Utils.qFromFloat(tf.getTrackingObjects().get(nTrackedObject)
					.getObjectQuaternion());
		return null;
	}

	@Override
	public boolean canExtract(TrackingFrame tf) {
		return tf.getTrackingObjects().containsKey(nTrackedObject);
	}
	
	/**
	 * uses given {@link TrackingFrameExtractor} to extract data from evenly
	 * spaced points along the path (given as List of {@link TrackingFrame}s).
	 * 
	 * @param numPositions
	 *            how many points along the path from which to get the
	 *            information. Data is regular intervals over the distance of
	 *            the path
	 * @param posTrackedObject
	 *            the trackedObject name which will be used to the path
	 *            distance.
	 * @param healthyMotion2
	 *            the motion data
	 * @param ext
	 *            a {@link TrackingFrameExtractor} which will be used on the
	 *            TrackingFrames to extract the information
	 * @return a list of length numPositions, containing the data extracted by
	 *         ext
	 * @throws Exception
	 *             if there are < numPositions frames in the given motion data
	 */
	/*private <T> List<T> getTrackingObjectInformationAtProgresses(int numPositions,
			String posTrackedObject, List<TrackingFrame> healthyMotion2,
			TrackingFrameExtractor<T, TrackingFrame> ext) throws Exception {
		if (healthyMotion2.size() < numPositions)
			throw new Exception("not enough TrackingFrame to calculate rotations. expected > "
					+ numPositions + " . was " + healthyMotion2.size());
		List<Vector3f> m = Utils.toVectors(healthyMotion2, posTrackedObject);
		Vector3f cur = Utils.pFromFloat(healthyMotion2.get(1).getTrackingObjects()
				.get(posTrackedObject).getObjectPoint()), prev = Utils.pFromFloat(healthyMotion2
				.get(0).getTrackingObjects().get(posTrackedObject).getObjectPoint());
		int curPosition = 0;
		Vector3f from = m.get(0);
		Vector3f furthest = AbstractReachDeviationControl.findFurthest(from, m);
		float totalDist = Utils.calculatePathDistanceTo(m, furthest);
		LOG.debug("calculated total distance: " + totalDist + ". from: " + from
				+ " to furthest point: " + furthest);
		float distTravelled = 0;
		List<T> qs = new ArrayList<T>(numPositions);
		for (int i = 0; i < healthyMotion2.size() && curPosition < numPositions; i++) {
			if (ext.canExtract(healthyMotion2.get(i))) {
				if (healthyMotion2.get(i).getTrackingObjects().containsKey(posTrackedObject))
					cur = Utils.pFromFloat(healthyMotion2.get(i).getTrackingObjects()
							.get(posTrackedObject).getObjectPoint());
			} else
				// skip frames without the desired TrackingObject, slash that
				// can't be extracted
				continue;
			distTravelled += cur.subtract(prev).length();
			float comp = distTravelled / totalDist;
			float prog = (float) curPosition / ((float) numPositions - 1);
			if (comp >= prog) {
				LOG.trace("comp: " + comp + " curPosition/numPositions: " + prog
						+ " extracing value and adding to return list");
				qs.add(ext.extract(healthyMotion2.get(i)));
				curPosition++;
			}
			prev = cur;
		}
		return qs;
	}9*/



// This is used when we return to the rest position
/*public void onRestChange(boolean isRestingNow) {
	if (isRestingNow) {
		// if we are now resting. If we were reaching previously,
		// end recording. Otherwise start recording, in preperation
		// for another reach
		LOG.debug("resting now");
		try {
			if (isReaching) {
				LOG.debug("we were reaching. ending recording: " + trialId);
				assert (trialId != null);
				// end reach. stop recording
				mMcwc.stopRecording(trialId);
				// Is this something I need
				//Utils.setText(screen, statusTextId, strings.getString("status") + ": "
				//		+ strings.getString("recordingComplete"));
				isReaching = false;
				if (game_status.rep == game_status.num_reps) {
					// set complete
					game_status.rep = 0;
					game_status.level++;
					LOG.info("completed trial " + game_status.rep + "/" + game_status.num_reps + ". Set complete!");
					if (game_status.level == game_status.num_levels) {
						LOG.info("completed set " + game_status.level + "/" + game_status.num_levels + ". Session complete");
						System.exit(1);
					}
					initSetParams(game_status.level);
					// curSetConfig = setConfigurations.get(set);
					// LOG.debug("loading configuration for set: " + set +
					// " config: "
					// + curSetConfig);
				}
				//startScreenAppState.updateGameGUI(score, trial, set, numTrials, numSets);

			} else {
				// not reaching. start reach
				assert (setId != null);
				trialId = mMcwc.startRecording(setId, Configuration.getTrackedType());
				LOG.debug("not reaching, and now resting. Begginning reach phase. starting recording. TrialId: "
						+ trialId);
				//Utils.setText(screen, statusTextId, strings.getString("status") + ": "
				//		+ strings.getString("recording"));
				isReaching = true;
				game_status.rep++;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
}*/
