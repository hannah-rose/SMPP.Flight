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

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
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
public class GameRunning extends AbstractAppState implements ScreenController {
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
    
    private List<TrackingFrame> healthyMotion;
    private UUID experimentId, setId, trialId;
    private IFrameObservable o;
    
    List<SetConfig> setConfigurations;
    MotionCaptureWebClient mMcwc;
    String nTrackedObject = Configuration.getNormTrackedObject();
    SetConfig curSetConfig;
    
    /*
     * Enum to set control source for game
     * TODO: set from start screen
     */
    enum Control {
    	USER, PLAYBACK, KEYS
    }
    
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
    Control control = Control.PLAYBACK;
    
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
        try {
        	mMcwc = new MotionCaptureWebClient(Configuration.getSMPPWebServiceURI());
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        
        // Configurations
        try {
        	setConfigurations = GameConfiguration.getSetListForId(Configuration.getUserId());
        	//initSetParams(game_status.level);
        }
        catch (IOException e) {
        	// couldn't find sesion configuration for configured userId
        	e.printStackTrace();
        } //catch (URISyntaxException e) {
			//e.printStackTrace();
		//}
        
        // Initialize control
        switch(control) {
        case USER:
        	// Live motion tracking
        	try {
        		UserMotionControl userMotionControl = new UserMotionControl(Configuration.getTrackedObject());
        		flightgame.player.addControl(userMotionControl);
        		LOG.trace("User control activated");
        	} catch (Exception e1) {
        		e1.printStackTrace();
        	}
        case PLAYBACK:
        	// Playback control of a recorded reach
        	try {
    			MotionPlaybackController playbackControl = new MotionPlaybackController("RIGHT_HAND_1", healthyMotion);
    			flightgame.player.addControl(playbackControl);
    			LOG.trace("Motion control activated");
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
        case KEYS:
        	// Use the keyboard to control the plane
        	try {
        		key_motion = new Key_Motion(flightgame.player, cam);
        		stateManager.attach(key_motion);
        		LOG.trace("Keyboard control activated");
        	} catch (Exception e1) {
    			e1.printStackTrace();
    		}
        }
         
        // Attach states
        stateManager.attach(game_status);
        stateManager.attach(physics);
        stateManager.attach(sound);
    }

	@Override
	public void bind(Nifty arg0, Screen arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndScreen() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartScreen() {
		// TODO Auto-generated method stub
		
	}


private void initSetParams(int set) throws ClientProtocolException, URISyntaxException, IOException 
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
