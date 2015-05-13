/*
 * Initializes the game world and controls its level progression
 * Has several subordinate classes and states
 */
package smpp.game.appstates;

import com.jme3.scene.Node;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.Application;
import com.jme3.input.InputManager;
import com.jme3.asset.AssetManager;
import com.jme3.renderer.ViewPort;
import com.jme3.input.FlyByCamera;
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;

import com.jme3.app.SimpleApplication;
import smpp.game.*;
import smpp.game.control.*;
import smpp.game.display.*;
import smpp.game.effects.*;

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
    
    @Override
    public void update(float tpf){
        
    }
    
    @Override
    public void cleanup() {}
    
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
        
        key_motion = new Key_Motion(flightgame.player, cam);
        game_status = new Status(assetManager,settings);
        sound = new Sound(assetManager);
        physics = new GamePhysics(stateManager, world, flightgame.player, game_status, sound);
        
        stateManager.attach(game_status);
        stateManager.attach(key_motion);
        stateManager.attach(physics);
        stateManager.attach(sound);
    }
}
